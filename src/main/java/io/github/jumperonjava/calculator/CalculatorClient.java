package io.github.jumperonjava.calculator;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.command.v2.ClientCommandRegistrationCallback;
import net.fabricmc.fabric.api.client.command.v2.FabricClientCommandSource;
import net.minecraft.client.MinecraftClient;
import net.minecraft.command.CommandRegistryAccess;
import net.minecraft.text.Text;
import net.objecthunter.exp4j.ExpressionBuilder;
import net.objecthunter.exp4j.function.Function;

import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.argument;
import static net.fabricmc.fabric.api.client.command.v2.ClientCommandManager.literal;

public class CalculatorClient implements ClientModInitializer{
    public void onInitializeClient(){
        ClientCommandRegistrationCallback.EVENT.register(this::regCommand);
    }

    private void regCommand(CommandDispatcher<FabricClientCommandSource> dispatcher, CommandRegistryAccess registryAccess) {
        var builder = literal("cal").then(
                argument("expression",StringArgumentType.greedyString()).executes(this::calculate)
        ).executes(this::failNoExp);
        dispatcher.register(builder);
    }

    private int failNoExp(CommandContext<FabricClientCommandSource> fabricClientCommandSourceCommandContext) {
        sendChatMessage("No expression specified");
        return 0;
    }

    private int calculate(CommandContext<FabricClientCommandSource> fabricClientCommandSourceCommandContext) {
        try{
            var expressionString = fabricClientCommandSourceCommandContext.getArgument("expression",String.class);
            var expression = new ExpressionBuilder(expressionString);
            expression.function(new Function("round",1) {
                @Override
                public double apply(double... args) {
                    return Math.round(args[0]);
                }
            });
            var result = expression.build().evaluate();
            if(result == (int)result)
                sendChatMessage("Result: ",(String.valueOf((int) result)));
            else
                sendChatMessage("Result: ",result);
        }
        catch (Exception e){
            sendChatMessage("Error while evaluating: ", e.getMessage());
        }

        return 0;
    }
    public static void sendChatMessage(Object... messages)
    {
        StringBuilder fullMessage= new StringBuilder();
        for(var message : messages)
            fullMessage.append((message == null) ? "null" : message + " ");
        MinecraftClient.getInstance().inGameHud.getChatHud().addMessage(Text.literal(fullMessage.toString()));
    }
}
