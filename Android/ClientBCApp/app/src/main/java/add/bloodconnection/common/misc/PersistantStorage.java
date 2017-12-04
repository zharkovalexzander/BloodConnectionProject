package add.bloodconnection.common.misc;

import android.content.Context;
import android.content.SharedPreferences;

/**
 * Created by Alexzander on 03.12.2017.
 */

public class PersistantStorage {
    public static final String STORAGE_NAME = "app";

    private static SharedPreferences settings = null;
    private static SharedPreferences.Editor editor = null;
    private static Context context = null;

    public static void init(Context cntxt){
        context = cntxt;
    }

    private static void init(){
        settings = context.getSharedPreferences(STORAGE_NAME, Context.MODE_PRIVATE);
        editor = settings.edit();
    }

    public static void addProperty( String name, String value ){
        if( settings == null ){
            init();
        }
        editor.putString( name, value );
        editor.apply();
    }

    public static String getProperty( String name ){
        if( settings == null ){
            init();
        }
        return settings.getString( name, null );
    }
}
