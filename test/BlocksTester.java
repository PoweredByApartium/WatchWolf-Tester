import com.rogermiranda1000.watchwolf.entities.Position;
import com.rogermiranda1000.watchwolf.entities.blocks.Block;
import com.rogermiranda1000.watchwolf.entities.blocks.Blocks;
import com.rogermiranda1000.watchwolf.entities.blocks.Directionable;
import com.rogermiranda1000.watchwolf.entities.blocks.Orientable;
import com.rogermiranda1000.watchwolf.entities.blocks.special.Bell;
import com.rogermiranda1000.watchwolf.tester.AbstractTest;
import com.rogermiranda1000.watchwolf.tester.TesterConnector;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ArgumentsSource;

import java.util.ArrayList;

import static org.junit.jupiter.api.Assertions.*;

@ExtendWith(BlocksTester.class) // run the tests with the AbstractTest overridden methods
public class BlocksTester extends AbstractTest {
    @Override
    public String getConfigFile() {
        return "config.yaml";
    }

    @ParameterizedTest
    @ArgumentsSource(BlocksTester.class)
    public void setBlock(TesterConnector connector) throws Exception {
        Position p = new Position("world", 0,0,0);
        connector.server.setBlock(p, Blocks.IRON_BLOCK);
        assertEquals(connector.server.getBlock(p), Blocks.IRON_BLOCK);
    }

    @ParameterizedTest
    @ArgumentsSource(BlocksTester.class)
    public void setComplexBlock(TesterConnector connector) throws Exception {
        Position p = new Position("world", 0,0,0);
        connector.server.setBlock(p, Blocks.OAK_SLAB);
        assertEquals(connector.server.getBlock(p), Blocks.OAK_SLAB);
    }

    @ParameterizedTest
    @ArgumentsSource(BlocksTester.class)
    public void setChangedOrientableBlock(TesterConnector connector) throws Exception {
        Position p = new Position("world", 0,0,0);
        Block slab = (Block) Blocks.ACACIA_SLAB.setOrientation(Orientable.Orientation.U, true);
        connector.server.setBlock(p, slab);
        Block get = connector.server.getBlock(p);

        ArrayList<Byte> originalData = new ArrayList<>(),
                gettedData = new ArrayList<>();
        slab.sendSocketData(originalData);
        get.sendSocketData(gettedData);
        assertEquals(originalData, gettedData);
    }

    @ParameterizedTest
    @ArgumentsSource(BlocksTester.class)
    public void setChangedOrientableDirectionableBlock(TesterConnector connector) throws Exception {
        Position p = new Position("world", 0,0,0);
        Bell bell = (Bell) Blocks.BELL.setOrientation(Orientable.Orientation.W, true);
        bell = (Bell) bell.setDirection(Directionable.Direction.SINGLE_WALL);
        connector.server.setBlock(p, bell);
        Block get = connector.server.getBlock(p);

        ArrayList<Byte> originalData = new ArrayList<>(),
                gettedData = new ArrayList<>();
        bell.sendSocketData(originalData);
        get.sendSocketData(gettedData);
        assertEquals(originalData, gettedData);
    }
}
