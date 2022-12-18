package dev.watchwolf.entities.blocks.special;

import dev.watchwolf.entities.blocks.*;
import dev.watchwolf.entities.SocketHelper;
import dev.watchwolf.entities.blocks.Block;

import java.util.*;

public class DeadBubbleCoral extends Block {

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
	public DeadBubbleCoral(short id) {
		super(id, "DEAD_BUBBLE_CORAL");
	}

	public DeadBubbleCoral(int id) {
		this((short) id);
	}

	private DeadBubbleCoral(DeadBubbleCoral old) {
		this(old.id);
	}

}