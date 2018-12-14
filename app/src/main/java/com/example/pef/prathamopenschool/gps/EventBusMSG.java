/**
 * EventBusMSG - Java stdClass for Android
 * Created by G.Capelli (BasicAirData) on 05/08/17.
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program. If not, see <http://www.gnu.org/licenses/>.
 */

package com.example.pef.prathamopenschool.gps;

public class EventBusMSG {

    public static final short APP_RESUME                       =   1;  // Sent to components on app resume
    public static final short APP_PAUSE                        =   2;  // Sent to components on app pause
    public static final short NEW_TRACK                        =   3;  // Request to create a new track
    public static final short UPDATE_FIX                       =   4;  // Notify that a new fix is available
    public static final short UPDATE_TRACK                     =   5;  // Notify that the current track stats are updated
    public static final short UPDATE_TRACKLIST                 =   6;  // Notify that the tracklist is changed
    public static final short UPDATE_SETTINGS                  =   7;  // Tell that settings are changed
    public static final short REQUEST_ADD_PLACEMARK            =   8;  // The user ask to add a placemark
    public static final short ADD_PLACEMARK                    =   9;  // The placemark is available
    public static final short APPLY_SETTINGS                   =  10;  // The new settings must be applied
    public static final short DELETE_TRACK                     =  20;  // Delete the track (given id)
    public static final short EXPORT_TRACK                     =  21;  // Export the track (given id)
    public static final short VIEW_TRACK                       =  22;  // View the track (given id)
    public static final short SHARE_TRACK                      =  23;  // Share the track (given id)
    public static final short TRACK_EXPORTED                   =  24;  // The exporter has finished to process the track (given id)
    public static final short TRACKLIST_SELECTION              =  25;  // The user select (into the tracklist) the track with a given id
    public static final short INTENT_SEND                      =  26;  // Request to
    public static final short TOAST_UNABLE_TO_WRITE_THE_FILE   =  27;  // Exporter fails to export the Track (given id)

    static final short TRACK_SETPROGRESS                =  30;  // Change the progress value of the Track (given id)
}
