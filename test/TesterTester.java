import com.rogermiranda1000.watchwolf.entities.Container;
import com.rogermiranda1000.watchwolf.entities.Position;
import com.rogermiranda1000.watchwolf.entities.blocks.Block;
import com.rogermiranda1000.watchwolf.entities.blocks.Blocks;
import com.rogermiranda1000.watchwolf.entities.entities.DroppedItem;
import com.rogermiranda1000.watchwolf.entities.entities.Entity;
import com.rogermiranda1000.watchwolf.entities.entities.EntityType;
import com.rogermiranda1000.watchwolf.entities.items.Item;
import com.rogermiranda1000.watchwolf.entities.items.ItemType;
import com.rogermiranda1000.watchwolf.tester.AbstractTest;
import com.rogermiranda1000.watchwolf.tester.ExtendedClientPetition;
import com.rogermiranda1000.watchwolf.tester.TesterConnector;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

/**
 * Alright... We're doing a Tester to test plugins, but who tests the tester to test plugins?
 * That's right! We need a Tester for the Tester.
 */
@ExtendWith(TesterTester.class) // run the tests with the AbstractTest overridden methods
public class TesterTester extends AbstractTest {
    @Override
    public File getConfigFile() {
        return null; // TODO
    }

    @ParameterizedTest
    @ArgumentsSource(TesterTester.class)
    public void opPlayer(TesterConnector connector) throws Exception {
        String clientName = connector.getClients()[0];
        connector.opPlayer(clientName);

        connector.getClientPetition(0).runCommand("kick " + clientName);

        assertFalse(Arrays.asList(connector.getPlayers()).contains(clientName));
    }

    @ParameterizedTest
    @ArgumentsSource(TesterTester.class)
    public void whitelistPlayer(TesterConnector connector) throws Exception {
        // whitelist is automatic; just see if the player is online
        assertTrue(Arrays.asList(connector.getPlayers()).contains(connector.getClients()[0]));
    }

    @ParameterizedTest
    @ArgumentsSource(TesterTester.class)
    public void runCommand(TesterConnector connector) throws Exception {
        final String clientName = connector.getClients()[0];
        connector.runCommand("kick " + clientName);

        assertFalse(Arrays.asList(connector.getPlayers()).contains(clientName));
    }

    @ParameterizedTest
    @ArgumentsSource(TesterTester.class)
    public void getInventory(TesterConnector connector) throws Exception {
        String clientName = connector.getClients()[0];

        Item give = new Item(ItemType.BEDROCK);
        connector.giveItem(clientName, give);

        Container inv = connector.getInventory(clientName);
        HashMap<ItemType,Integer> items = TesterTester.getAmounts(inv.getItems());

        assertEquals(1, items.size()); // we expect only one item (BEDROCK x1)
        assertEquals(1, items.get(ItemType.BEDROCK)); // we expect one item of BEDROCK
    }

    private static HashMap<ItemType,Integer> getAmounts(Item ...items) {
        HashMap<ItemType,Integer> r = new HashMap<>();

        for (Item i : items) {
            if (i == null) continue;

            Integer acum = r.get(i.getType());
            if (acum == null) r.put(i.getType(), (int)i.getAmount());
            else r.put(i.getType(), acum + i.getAmount());
        }

        return r;
    }

    /**
     * Break a dirt block far from the player, and then see if there's a dirt block dropped
     */
    @ParameterizedTest
    @ArgumentsSource(TesterTester.class)
    public void getDrops(TesterConnector connector) throws Exception {
        ExtendedClientPetition clientConnector = connector.getClientPetition(0);

        Position pos = clientConnector.getPosition().add(0, 1, 0);
        int farBlock = 3;

        // place block
        for (int n = 0; n < farBlock-1; n++) {
            connector.setBlock(pos, Blocks.AIR);
            pos.add(1, 0, 0);
        }
        connector.setBlock(pos, Blocks.DIRT);

        // break block
        clientConnector.breakBlock(pos);

        Entity []entities = connector.getEntities(pos, 10);
        List<Item> items = new ArrayList<>();
        for (Entity e : entities) {
            if (e.getType().equals(EntityType.DroppedItem)) items.add(((DroppedItem)e).getItem());
        }

        HashMap<ItemType,Integer> it = TesterTester.getAmounts(items.toArray(new Item[0]));
        System.out.println("Found " + it.toString());
        assertTrue(it.get(ItemType.DIRT) >= 1);
    }
}
