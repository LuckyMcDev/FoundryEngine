package com.example.dep

import net.minecraft.server.level.ServerPlayer

/**
 * A class, which can handle stuff itself, but is not an entrypoint.
 * This is useful for code organization, and for sharing code between different entrypoints.
 */
class Dependency {
    static void hello(ServerPlayer player) {
        //println("hello from dep v2")
		//println("Hello v2 from editor!")
		
		// THIS SHOULD NOW JUST FUCK IT UP KINDA
    }
}
