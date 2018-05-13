package com.pcb.pcbridge.spigot.warnings.repositories;

public class WarningRepository {

    public void get() {
//        String selectQuery = "SELECT *,"
//                + " t1." + PlayerContract.TablePlayers.COL_ALIAS + " AS playerAlias,"
//                + " t1." + PlayerContract.TablePlayers.COL_UUID + " AS playerUuid,"
//                + " t2." + PlayerContract.TablePlayers.COL_ALIAS + " AS staffAlias,"
//                + " t2." + PlayerContract.TablePlayers.COL_UUID + " AS staffUuid"
//                + " FROM " + WarningContract.TableWarnings.TABLE_NAME + " AS s"
//                + " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t1"
//                + " ON s." + WarningContract.TableWarnings.COL_PLAYER_ID + " = t1." + PlayerContract.TablePlayers._ID
//                + " LEFT JOIN " + PlayerContract.TablePlayers.TABLE_NAME + " AS t2"
//                + " ON s." + WarningContract.TableWarnings.COL_STAFF_ID + " = t2." + PlayerContract.TablePlayers._ID
//                + " WHERE t1." + PlayerContract.TablePlayers.COL_UUID + " = ?"
//                + " ORDER BY s." + WarningContract.TableWarnings.COL_TIMESTAMP + " DESC";
//
//        try(PreparedStatement stmt = conn.prepareStatement(selectQuery)) {
//            stmt.setString(1, uuid.toString());
//            ResultSet results = stmt.executeQuery();
//
//            if(results.first())
//            {
//                do
//                {
//                    WarningModel warning = new WarningModel();
//                    warning.PlayerName 	= results.getString("playerAlias");
//                    warning.PlayerUUID 	= results.getString("playerUuid");
//                    warning.StaffName 	= results.getString("staffAlias");
//                    warning.StaffUUID 	= results.getString("staffUuid");
//                    warning.Reason 		= results.getString(WarningContract.TableWarnings.COL_REASON);
//                    warning.Timestamp 	= results.getLong(WarningContract.TableWarnings.COL_TIMESTAMP);
//
//                    warnings.add(warning);
//                }
//                while(results.next());
//            }
//        }
    }

}
