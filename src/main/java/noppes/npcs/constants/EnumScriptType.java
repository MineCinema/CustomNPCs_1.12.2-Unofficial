package noppes.npcs.constants;

public enum EnumScriptType
{
	INIT("init"), 
	TICK("tick"), 
	INTERACT("interact"), 
	DIALOG("dialog"), 
	DAMAGED("damaged"), 
	DIED("died"), 
	ATTACK_MELEE("meleeAttack"), 
	TARGET("target"), 
	COLLIDE("collide"), 
	KILL("kill"), 
	DIALOG_OPTION("dialogOption"), 
	TARGET_LOST("targetLost"), 
	ROLE("role"), 
	RANGED_LAUNCHED("rangedLaunched"), 
	CLICKED("clicked"), 
	FALLEN_UPON("fallenUpon"), 
	RAIN_FILLED("rainFilled"), 
	BROKEN("broken"), 
	HARVESTED("harvested"), 
	EXPLODED("exploded"), 
	NEIGHBOR_CHANGED("neighborChanged"), 
	REDSTONE("redstone"), 
	DOOR_TOGGLE("doorToggle"), 
	TIMER("timer"), 
	TOSS("toss"), 
	CONTAINER_OPEN("containerOpen"), 
	CONTAINER_CLOSED("containerClosed"), 
	LOGIN("login"), 
	LOGOUT("logout"), 
	CHAT("chat"), 
	DAMAGED_ENTITY("damagedEntity"), 
	DIALOG_CLOSE("dialogClose"), 
	SPAWN("spawn"), 
	TOSSED("tossed"), 
	PICKEDUP("pickedUp"), 
	PICKUP("pickUp"), 
	ATTACK("attack"), 
	PROJECTILE_TICK("projectileTick"), 
	PROJECTILE_IMPACT("projectileImpact"), 
	FACTION_UPDATE("factionUpdate"), 
	LEVEL_UP("levelUp"), 
	QUEST_START("questStart"), 
	QUEST_COMPLETED("questCompleted"), 
	QUEST_TURNIN("questTurnIn"), 
	KEY_UP("keyPressed"), 
	CUSTOM_CHEST_CLOSED("customChestClosed"), 
	CUSTOM_CHEST_CLICKED("customChestClicked"), //
	SCRIPT_COMMAND("scriptCommand"), 
	CUSTOM_GUI_CLOSED("customGuiClosed"), 
	CUSTOM_GUI_BUTTON("customGuiButton"), 
	CUSTOM_GUI_SLOT("customGuiSlot"), 
	CUSTOM_GUI_SCROLL("customGuiScroll"), 
	CUSTOM_GUI_SLOT_CLICKED("customGuiSlotClicked"),
	QUEST_CANCELED("questCanceled"), 
	ITEM_FISHED("itemFished"), 
	ITEM_CRAFTED("itemCrafted"), 
	KEY_DOWN("keyDown"), 
	MOUSE_DOWN("mouseDown"), 
	MOUSE_UP("mousePressed"), 
	POTION_IS_READY("isReady"), 
	POTION_PERFORM("performEffect"), 
	POTION_AFFECT("affectEntity"), 
	PLASED("plased"), 
	POTION_END("endEffect"), 
	SCRIPT_TRIGGER("trigger"), 
	SOUND_PLAY("soundPlayed"), 
	SOUND_STOP("soundStoped"), 	
	PACKEGE_RECEIVED("packageReceived"), 	
	PACKEGE_FROM("packageFrom"),
	CUSTOM_TELEPORT("customTeleport"),
	KEY_ACTIVE("keyActive"),
	STOP_ANIMATION("stopAnimation"),
	GUI_OPEN("openGUI"),
	SOUND_TICK_EVENT("soundTickEvent");
	
	public String function;
	
	private EnumScriptType(String function) { this.function = function; }
	
}
