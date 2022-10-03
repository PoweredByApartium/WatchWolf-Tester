package com.rogermiranda1000.watchwolf.entities.blocks.special;

import com.rogermiranda1000.watchwolf.entities.blocks.*;
import com.rogermiranda1000.watchwolf.entities.SocketHelper;
import java.util.*;

public class ZombieHead extends Block {

	/*   --- CONSTRUCTORS ---   */
	public ZombieHead(short id) {
		super(id, "ZOMBIE_HEAD");
	}

	public ZombieHead(int id) {
		this((short) id);
	}

	private ZombieHead(ZombieHead old) {
		this(old.id);
	}

}