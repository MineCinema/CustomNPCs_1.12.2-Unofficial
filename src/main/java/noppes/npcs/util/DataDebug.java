package noppes.npcs.util;

import java.util.Map;

import com.google.common.collect.Maps;

import net.minecraft.entity.player.EntityPlayer;
import noppes.npcs.CustomNpcs;

public class DataDebug {

	public class Debug {

		public long max = 0L;
		Map<String, Long> starteds = Maps.newHashMap(); // temp time [Name, start time]
		public Map<String, Map<String, Long[]>> times = Maps.newHashMap(); // Name, [count, all time in work]

		public void end(String eventName, String eventTarget) {
			if (eventName == null || eventTarget == null) {
				return;
			}
			String key = eventName + "_" + eventTarget;
			if (!this.starteds.containsKey(key) || this.starteds.get(key) <= 0L) {
				return;
			}
			if (!this.times.containsKey(eventName)) {
				this.times.put(eventName, Maps.newHashMap());
			}
			if (!this.times.get(eventName).containsKey(eventTarget)) {
				this.times.get(eventName).put(eventTarget, new Long[] { 0L, 0L });
			}

			Long[] arr = this.times.get(eventName).get(eventTarget);
			arr[0]++;
			long r = System.currentTimeMillis() - this.starteds.get(key);
			arr[1] += r;
			if (this.max < arr[1]) {
				this.max = arr[1];
			}
			this.times.get(eventName).put(eventTarget, arr);
			this.starteds.put(key, 0L);
		}

		public void start(String eventName, String eventTarget) {
			if (eventName == null || eventTarget == null) {
				return;
			}
			String key = eventName + "_" + eventTarget;
			if (!this.starteds.containsKey(key)) {
				this.starteds.put(key, 0L);
			}

			if (this.starteds.get(key) > 0L) {
				return;
			}
			this.starteds.put(key, System.currentTimeMillis());
		}
	}

	public Map<String, Debug> data = Maps.newHashMap();

	public void endDebug(String side, Object target, String classMetod) {
		if (!CustomNpcs.VerboseDebug) { return; }
		try {
			if (!this.data.containsKey(side)) {
				return;
			}
			String key = target == null ? "Mod"
					: (target instanceof String) ? (String) target
							: (target instanceof EntityPlayer) ? "Players" : "MOBs";
			this.data.get(side).end(classMetod, key);
		} catch (Exception e) {
		}
	}

	public void startDebug(String side, Object target, String classMetod) {
		if (!CustomNpcs.VerboseDebug) { return; }
		try {
			if (!this.data.containsKey(side)) {
				this.data.put(side, new Debug());
			}
			String key = target == null ? "Mod"
					: (target instanceof String) ? (String) target
							: (target instanceof EntityPlayer) ? "Players" : "MOBs";
			this.data.get(side).start(classMetod, key);
		} catch (Exception e) {
		}
	}
	
	public void stopAll() {
		if (!CustomNpcs.VerboseDebug) { return; }
		for (String side: this.data.keySet()) {
			for (String k: this.data.get(side).starteds.keySet()) {
				this.data.get(side).end(k.substring(0, k.indexOf('_')), k.substring(k.indexOf('_')+1));
			}
		}
	}
}
