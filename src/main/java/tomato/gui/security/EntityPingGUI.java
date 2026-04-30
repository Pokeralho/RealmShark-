package tomato.gui.security;

import tomato.backend.data.TomatoData;
import tomato.gui.maingui.CustomListGUI;

import java.util.ArrayList;

public class EntityPingGUI extends CustomListGUI {

    public EntityPingGUI(TomatoData data) {
        super(data, "entityIdPings", "Entity ID Alerts", getEntityIdPings(data), "Entity ID");
        this.validationErrorMessage = "Invalid Entity ID! Must be a number.";
    }

    private static ArrayList<String> getEntityIdPings(TomatoData data) {
        // We need to access getEntityIdPings from TomatoData
        // If it doesn't exist, we should add it.
        return data.getEntityIdPings();
    }

    @Override
    protected boolean validateEntry(String entry) {
        try {
            Integer.parseInt(entry.trim());
            return true;
        } catch (NumberFormatException e) {
            return false;
        }
    }
}
