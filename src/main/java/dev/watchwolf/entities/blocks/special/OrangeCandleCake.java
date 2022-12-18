package dev.watchwolf.entities.blocks.special;

import dev.watchwolf.entities.blocks.*;
import dev.watchwolf.entities.SocketHelper;
import dev.watchwolf.entities.blocks.Block;

import java.util.*;

public class OrangeCandleCake extends Block {

	/*   --- SOCKET DATA OVERRIDE ---   */
	@Override
	public void sendSocketData(ArrayList<Byte> out) {
		SocketHelper.addShort(out, this.id);
		out.add((byte)0);
		out.add((byte)0);
		out.add((byte)0);
		SocketHelper.fill(out, 51);
	}

	/*   --- CONSTRUCTORS ---   */
	public OrangeCandleCake(short id) {
		super(id, "ORANGE_CANDLE_CAKE");
	}

	public OrangeCandleCake(int id) {
		this((short) id);
	}

	private OrangeCandleCake(OrangeCandleCake old) {
		this(old.id);
	}

}