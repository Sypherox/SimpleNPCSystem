package Sypherox.data;

import org.bukkit.Location;
import java.util.ArrayList;
import java.util.List;

public class NPC {

    private final String id;
    private Location location;
    private String skinTexture;
    private String skinSignature;
    private boolean mirrorSkin;
    private List<String> hologramLines;
    private NPCAction action;
    private String actionValue;
    private boolean eyeContact;

    public NPC(String id, Location location) {
        this.id = id;
        this.location = location;
        this.skinTexture = null;
        this.skinSignature = null;
        this.mirrorSkin = false;
        this.hologramLines = new ArrayList<>();
        this.action = NPCAction.NONE;
        this.actionValue = "";
        this.eyeContact = false;
    }

    public String getId() {
        return id;
    }

    public Location getLocation() {
        return location;
    }

    public void setLocation(Location location) {
        this.location = location;
    }

    public String getSkinTexture() {
        return skinTexture;
    }

    public void setSkinTexture(String skinTexture) {
        this.skinTexture = skinTexture;
    }

    public String getSkinSignature() {
        return skinSignature;
    }

    public void setSkinSignature(String skinSignature) {
        this.skinSignature = skinSignature;
    }

    public boolean isMirrorSkin() {
        return mirrorSkin;
    }

    public void setMirrorSkin(boolean mirrorSkin) {
        this.mirrorSkin = mirrorSkin;
        if (mirrorSkin) {
            this.skinTexture = null;
            this.skinSignature = null;
        }
    }

    public List<String> getHologramLines() {
        return new ArrayList<>(hologramLines);
    }

    public void setHologramLines(List<String> lines) {
        this.hologramLines = new ArrayList<>();
        int maxLines = Math.min(lines.size(), 3);
        for (int i = 0; i < maxLines; i++) {
            this.hologramLines.add(lines.get(i));
        }
    }

    public void addHologramLine(String line) {
        if (hologramLines.size() < 3) {
            hologramLines.add(line);
        }
    }

    public void removeHologramLine(int index) {
        if (index >= 0 && index < hologramLines.size()) {
            hologramLines.remove(index);
        }
    }

    public void clearHologramLines() {
        hologramLines.clear();
    }

    public NPCAction getAction() {
        return action;
    }

    public void setAction(NPCAction action) {
        this.action = action;
    }

    public String getActionValue() {
        return actionValue;
    }

    public void setActionValue(String actionValue) {
        this.actionValue = actionValue;
    }

    public boolean hasEyeContact() {
        return eyeContact;
    }

    public void setEyeContact(boolean eyeContact) {
        this.eyeContact = eyeContact;
    }

    public enum NPCAction {
        NONE,
        COMMAND,
        GUI,
        SERVER
    }
}
