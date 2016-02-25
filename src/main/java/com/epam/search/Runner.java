package com.epam.search;

import com.epam.search.common.ConsoleParser;
import com.epam.search.services.GrabberService;
import com.epam.search.services.impl.GrabberServiceImpl;

import java.util.Optional;

import static com.epam.search.common.LoggingUtil.error;

/**
 * Created by Dmytro_Kovalskyi on 24.02.2016.
 */
public class Runner {
    public static void main(String[] args) {
        ConsoleParser parser = new ConsoleParser();
        ConsoleParser.ParseResult parseResult = parser.parse(args);
        if (!parseResult.isSuccess()) {
            error(Runner.class, "Illegal arguments");
            System.exit(1);
        }
        GrabberService service = new GrabberServiceImpl();
        Optional<String> name = parseResult.getName();
        if (name.isPresent())
            service.grab(name.get());
        else
            service.grab(parseResult.getFrom());
    }
}
