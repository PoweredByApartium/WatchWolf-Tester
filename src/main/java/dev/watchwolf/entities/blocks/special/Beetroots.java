package dev.watchwolf.entities.blocks.special;

import dev.watchwolf.entities.blocks.*;
import dev.watchwolf.entities.SocketHelper;
import dev.watchwolf.entities.blocks.Block;

import java.util.*;

public class Beetroots extends Block {

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
	public Beetroots(short id) {
		super(id, "BEETROOTS");
	}

	public Beetroots(int id) {
		this((short) id);
	}

	private Beetroots(Beetroots old) {
		this(old.id);
	}

}