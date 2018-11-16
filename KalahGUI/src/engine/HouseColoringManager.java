package engine;

import java.awt.Color;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import engine.config.CmapConfiguration;
import engine.config.CmapDefinition;
import game.Player;
import gui.Coordinator;
import gui.ActivityListener.Level;

public class HouseColoringManager {

	private static HouseColoringAgent north, south;
	private static Map<String, HouseColoringAgent> agents = new HashMap<>();

	static {
		HouseColoringAgent def = new HouseColoringAgent(new Uniform(Color.WHITE));
		addColoringAgent(def, "Default coloring agent");
		north = def;
		south = def;
	}

	public static HouseColoringAgent get(Player player) {
		return player.isSouth() ? south : north;
	}

	public static void setActive(HouseColoringAgent agent, Player player) {
		if (player.isSouth()) {
			south = agent;
		} else {
			north = agent;
		}
	}

	public static void addColoringAgent(HouseColoringAgent agent, String name) {
		agents.put(name, agent);
	}

	public static void fromConfiguration(CmapConfiguration conf) {
		//create uniform colormaps from the configuration 
		conf.getCmaps().stream().filter(e -> e.getType().equals(CmapDefinition.CmapType.UNIFORM.asString())).forEach(
				e -> addColoringAgent(new HouseColoringAgent(new Uniform(Color.decode(e.getColor())), Color.decode(conf.getFallback())), e.getName()));
		//create divergent colormaps from the configuration 
		conf.getCmaps().stream().filter(e -> e.getType().equals(CmapDefinition.CmapType.DIVERGENT.asString()))
				.forEach(e -> addColoringAgent(new HouseColoringAgent(new Divergent(e.getLower(), e.getUpper(),
						Color.decode(e.getGood()), Color.decode(e.getBad()), Color.decode(e.getNeutral())), Color.decode(conf.getFallback())),
						e.getName()));
		setActive(agents.get(conf.getSouth()), Player.ONE);
		setActive(agents.get(conf.getNorth()), Player.TWO);
	}
}
