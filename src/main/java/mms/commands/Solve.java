package mms.commands;

import static com.mojang.brigadier.arguments.StringArgumentType.string;
import static net.minecraft.server.command.CommandManager.argument;
import static net.minecraft.server.command.CommandManager.literal;

import java.math.BigDecimal;

import com.mojang.brigadier.Command;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.builder.LiteralArgumentBuilder;
import com.mojang.brigadier.context.CommandContext;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.udojava.evalex.Expression;

import org.apache.logging.log4j.Level;

import mms.main.MmsInit;
import net.fabricmc.fabric.api.command.v1.CommandRegistrationCallback;
import net.minecraft.server.command.ServerCommandSource;
import net.minecraft.text.LiteralText;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

public class Solve implements Command<ServerCommandSource> {
    private Solve() {
        CommandRegistrationCallback.EVENT.register((dispatcher, dedicated) -> {
            if (dedicated)
                MmsInit.log(Level.WARN, "/solve won't be registered on the server side");
            else
                dispatcher.register(getCommand());
        });

    }

    public static void register() {
        new Solve();
    }

    private LiteralArgumentBuilder<ServerCommandSource> getCommand() {
        return literal("solve").then(argument("expression", string()).executes(this));
    }

    @Override
    public int run(CommandContext<ServerCommandSource> ctx) throws CommandSyntaxException {
        String exp = StringArgumentType.getString(ctx, "expression");
        int code;
        Text response;
        try {
            BigDecimal result = new Expression(exp).eval();
            response = new LiteralText(String.format("= %s", result.toPlainString()));
            code = 0;
        } catch (Exception e) {
            response = new LiteralText(String.format("Error evaluating expression.\n\"%s\"", e.getMessage()))
                    .formatted(Formatting.RED);
            MmsInit.log(Level.ERROR, e.getMessage());
            code = -1;
        }
        ctx.getSource().sendFeedback(response, false);
        return code;
    }

}
