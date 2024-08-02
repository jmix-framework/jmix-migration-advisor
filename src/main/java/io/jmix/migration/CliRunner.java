package io.jmix.migration;

import com.beust.jcommander.JCommander;
import com.beust.jcommander.ParameterException;
import io.jmix.migration.command.AnalyzeCubaProjectCommand;
import io.jmix.migration.command.BaseCommand;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.HashMap;
import java.util.Map;

public class CliRunner {

    private static final Logger log = LoggerFactory.getLogger(CliRunner.class);

    public static void main(String[] args) {
        Map<String, BaseCommand> commands = new HashMap<>();
        commands.put("analyze-cuba", new AnalyzeCubaProjectCommand());

        JCommander.Builder commanderBuilder = JCommander.newBuilder();
        for (Map.Entry<String, BaseCommand> entry : commands.entrySet()) {
            commanderBuilder.addCommand(entry.getKey(), entry.getValue());
        }

        JCommander commander = commanderBuilder.build();

        try {
            commander.parse(args);
        } catch (ParameterException e) {
            commander.usage();
            return;
        }

        String parsedCommand = commander.getParsedCommand();

        if (parsedCommand == null) {
            commander.usage();
            return;
        }

        BaseCommand command = commands.get(parsedCommand);
        command.run();
    }
}