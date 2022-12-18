package dev.watchwolf.entities.blocks.special;

import dev.watchwolf.entities.blocks.*;
import dev.watchwolf.entities.SocketHelper;
import dev.watchwolf.entities.blocks.Block;

import java.util.*;

public class CandleCake extends Block {

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
	public CandleCake(short id) {
		super(id, "CANDLE_CAKE");
	}

	public CandleCake(int id) {
		this((short) id);
	}

	private CandleCake(CandleCake old) {
		this(old.id);
	}

}