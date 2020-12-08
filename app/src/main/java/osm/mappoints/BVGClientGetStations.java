package osm.mappoints;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.FormBody;
import okhttp3.MediaType;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.RequestBody;
import okhttp3.Response;

public class BVGClientGetStations implements Callback {

//    TODO integrate into okhttp3
//    link: https://bvg-app-hosting.systemtechnik-online.de:8030/swagger/ui/index#!/Search/Search_FindTariffPoints
//    Login username: 0bbffb55-8347-4d10-84d6-ef0a5dbb6f61@temp.local
//    Login password: Dh9uNjSk7vHTNIO33NTYNB3dfugsLsJw
//    Auhorization API KEY: Dh9uNjSk7vHTNIO33NTYNB3dfugsLsJw

    
    private static final String LOG_TAG = "bvgclientgetstations";
    private OkHttpClient okHttpClient;
    private ChooseStartingStationActivity activity;


    public BVGClientGetStations(ChooseStartingStationActivity activity) {
        this.activity = activity;
    }


    @Override
    public void onResponse(@NotNull Call call, @NotNull Response response) throws IOException {

    }

    @Override
    public void onFailure(@NotNull Call call, @NotNull IOException e) {

    }
}
