package me.sashie.skriptyaml.utils.yaml;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;

import org.bukkit.Location;
import org.bukkit.configuration.serialization.ConfigurationSerializable;
import org.bukkit.configuration.serialization.ConfigurationSerialization;
import org.bukkit.inventory.ItemStack;
import org.bukkit.util.Vector;
import org.yaml.snakeyaml.nodes.Node;
import org.yaml.snakeyaml.nodes.Tag;
import org.yaml.snakeyaml.representer.Represent;
import org.yaml.snakeyaml.representer.Representer;

import ch.njol.skript.aliases.ItemType;
import ch.njol.skript.util.Color;
import ch.njol.skript.util.Date;
import ch.njol.skript.util.Slot;
import ch.njol.skript.util.Time;
import ch.njol.skript.util.Timespan;
import ch.njol.skript.util.WeatherType;

public class SkriptYamlRepresenter extends Representer {

	private static List<String> representedClasses = new ArrayList<>();

	public SkriptYamlRepresenter() {
		this.nullRepresenter = new Represent() {
			@Override
			public Node representData(Object o) {
				return representScalar(Tag.NULL, "");
			}
		};

		this.representers.put(SkriptClass.class, new RepresentSkriptClass());
		this.representers.put(ItemType.class, new RepresentSkriptItemType());
		this.representers.put(Slot.class, new RepresentSkriptSlot());
		this.representers.put(Date.class, new RepresentSkriptDate());
		this.representers.put(Time.class, new RepresentSkriptTime());
		this.representers.put(Timespan.class, new RepresentSkriptTimespan());
		this.representers.put(Color.class, new RepresentSkriptColor());
		this.representers.put(WeatherType.class, new RepresentSkriptWeather());

		this.representers.put(Vector.class, new RepresentVector());
		this.representers.put(Location.class, new RepresentLocation());

		this.multiRepresenters.put(ConfigurationSerializable.class, new RepresentConfigurationSerializable());

		for (Class<?> c : representers.keySet()) {
			if (c != null)
				representedClasses.add(c.getSimpleName());
		}
	}

	public static boolean contains(Object object) {
		if (object == null)
			return false;
		return representedClasses.contains(object.getClass().getSimpleName());
	}

	private class RepresentConfigurationSerializable extends RepresentMap {
		@Override
		public Node representData(Object data) {
			return representConfigurationSerializable(data);
		}
	}

	private Node representConfigurationSerializable(Object data) {
		ConfigurationSerializable serializable = (ConfigurationSerializable) data;
		Map<String, Object> values = new LinkedHashMap<String, Object>();
		values.put(ConfigurationSerialization.SERIALIZED_TYPE_KEY,
				ConfigurationSerialization.getAlias(serializable.getClass()));
		values.putAll(serializable.serialize());

		return super.representData(values);
	}

	private class RepresentVector implements Represent {
		@Override
		public Node representData(Object data) {
			Map<String, Double> out = new LinkedHashMap<String, Double>();
			Vector vec = (Vector) data;
			out.put("x", vec.getX());
			out.put("y", vec.getY());
			out.put("z", vec.getZ());
			return representMapping(new Tag("!vector"), out, null);
		}
	}

	private class RepresentLocation implements Represent {
		@Override
		public Node representData(Object data) {
			Map<String, Object> out = new LinkedHashMap<String, Object>();
			Location loc = (Location) data;
			out.put("world", loc.getWorld().getName());
			out.put("x", loc.getX());
			out.put("y", loc.getY());
			out.put("z", loc.getZ());
			out.put("yaw", (double) loc.getYaw());
			out.put("pitch", (double) loc.getPitch());
			return representMapping(new Tag("!location"), out, null);
		}
	}

	private class RepresentSkriptClass extends RepresentMap {
		@Override
		public Node representData(Object data) {
			Map<String, Object> out = new LinkedHashMap<String, Object>();
			SkriptClass skriptClass = (SkriptClass) data;
			out.put("type", skriptClass.getType());
			out.put("data", skriptClass.getData());
			return representMapping(new Tag("!skriptclass"), out, null);
		}
	}

	private class RepresentSkriptItemType extends RepresentMap {
		@Override
		public Node representData(Object data) {
			ItemStack item = null;
			return representConfigurationSerializable(((ItemType) data).addTo(item));
		}
	}

	private class RepresentSkriptSlot extends RepresentMap {
		@Override
		public Node representData(Object data) {
			return representConfigurationSerializable(((Slot) data).getItem());
		}
	}

	/* TODO eventually add support for different slot types
	private class RepresentInventorySlot extends RepresentMap {
		@Override
		public Node representData(Object data) {
		Map<String, Object> out = new LinkedHashMap<String, Object>();
			InventorySlot slot = (InventorySlot) data;
			out.put("index", slot.getIndex());
			out.put("item", slot.getItem());
			return representMapping(new Tag("!skriptclass"), out, null);
		}
	}
	*/

	private class RepresentSkriptDate implements Represent {
		@Override
		public Node representData(Object data) {
			Calendar calendar = Calendar.getInstance(getTimeZone() == null ? TimeZone.getTimeZone("UTC") : timeZone);
			calendar.setTime(new java.util.Date(((Date) data).getTimestamp()));

			int years = calendar.get(Calendar.YEAR);
			int months = calendar.get(Calendar.MONTH) + 1; // 0..12
			int days = calendar.get(Calendar.DAY_OF_MONTH); // 1..31
			int hour24 = calendar.get(Calendar.HOUR_OF_DAY); // 0..24
			int minutes = calendar.get(Calendar.MINUTE); // 0..59
			int seconds = calendar.get(Calendar.SECOND); // 0..59
			int millis = calendar.get(Calendar.MILLISECOND);
			StringBuilder buffer = new StringBuilder(String.valueOf(years));
			while (buffer.length() < 4) {
				// ancient years
				buffer.insert(0, "0");
			}
			buffer.append("-");
			if (months < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(months));
			buffer.append("-");
			if (days < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(days));
			buffer.append("T");
			if (hour24 < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(hour24));
			buffer.append(":");
			if (minutes < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(minutes));
			buffer.append(":");
			if (seconds < 10) {
				buffer.append("0");
			}
			buffer.append(String.valueOf(seconds));
			if (millis > 0) {
				if (millis < 10) {
					buffer.append(".00");
				} else if (millis < 100) {
					buffer.append(".0");
				} else {
					buffer.append(".");
				}
				buffer.append(String.valueOf(millis));
			}

			// Get the offset from GMT taking DST into account
			int gmtOffset = calendar.getTimeZone().getOffset(calendar.get(Calendar.ERA), calendar.get(Calendar.YEAR),
					calendar.get(Calendar.MONTH), calendar.get(Calendar.DAY_OF_MONTH),
					calendar.get(Calendar.DAY_OF_WEEK), calendar.get(Calendar.MILLISECOND));
			if (gmtOffset == 0) {
				buffer.append('Z');
			} else {
				if (gmtOffset < 0) {
					buffer.append('-');
					gmtOffset *= -1;
				} else {
					buffer.append('+');
				}
				int minutesOffset = gmtOffset / (60 * 1000);
				int hoursOffset = minutesOffset / 60;
				int partOfHour = minutesOffset % 60;

				if (hoursOffset < 10) {
					buffer.append('0');
				}
				buffer.append(hoursOffset);
				buffer.append(':');
				if (partOfHour < 10) {
					buffer.append('0');
				}
				buffer.append(partOfHour);
			}

			return representScalar(new Tag("!skriptdate"), buffer.toString(), null);
		}
	}

	private class RepresentSkriptTime implements Represent {
		@Override
		public Node representData(Object data) {
			return representScalar(new Tag("!skripttime"), ((Time) data).toString(), null);
		}
	}

	private class RepresentSkriptTimespan implements Represent {
		@Override
		public Node representData(Object data) {
			return representScalar(new Tag("!skripttimespan"), ((Timespan) data).toString(), null);
		}
	}

	private class RepresentSkriptColor implements Represent {
		@Override
		public Node representData(Object data) {
			return representScalar(new Tag("!skriptcolor"), ((Color) data).toString(), null);
		}
	}

	private class RepresentSkriptWeather implements Represent {
		@Override
		public Node representData(Object data) {
			return representScalar(new Tag("!skriptweather"), ((WeatherType) data).toString().toLowerCase(), null);
		}
	}

}