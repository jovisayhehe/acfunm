package tv.avfun.test;

import java.io.IOException;

import tv.avfun.api.ApiParser;
import android.test.AndroidTestCase;


public class TestApi extends AndroidTestCase {
    public void testGetSinaMp4() throws Exception{

        try {

            //System.out.println(ApiParser.getSinaMp4("100110951"));
            System.out.println(ApiParser.getSinaMp4("102030885"));
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
