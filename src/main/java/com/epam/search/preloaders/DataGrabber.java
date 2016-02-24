package com.epam.search.preloaders;

import com.epam.search.services.GrabberService;
import com.epam.search.services.impl.GrabberServiceImpl;

/**
 * Created by Dmytro_Kovalskyi on 22.02.2016.
 */
public class DataGrabber {
    public static void main(String[] args) throws InterruptedException {
        GrabberService service = new GrabberServiceImpl();
      //  service.grab(0,100);
        service.grab(2700);
    }
}
