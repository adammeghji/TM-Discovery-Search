package com.epam.search;

import com.epam.search.services.GrabberService;
import com.epam.search.services.impl.GrabberServiceImpl;

/**
 * Created by Dmytro_Kovalskyi on 24.02.2016.
 */
public class Runner {
    public static void main(String[] args) {
        if (args.length != 1)
            throw new IllegalArgumentException("You should pass one param `from` run as java -jar search.jar 999");
        int from = Integer.parseInt(args[0]);
        GrabberService service = new GrabberServiceImpl();
        service.grab(from);
    }
}
