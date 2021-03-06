package com.tterrag.k9.commands;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

import com.google.common.collect.Lists;
import com.google.gson.reflect.TypeToken;
import com.tterrag.k9.K9;
import com.tterrag.k9.commands.api.Argument;
import com.tterrag.k9.commands.api.Command;
import com.tterrag.k9.commands.api.CommandContext;
import com.tterrag.k9.commands.api.CommandException;
import com.tterrag.k9.commands.api.CommandPersisted;
import com.tterrag.k9.commands.api.Flag;
import com.tterrag.k9.util.BakedMessage;
import com.tterrag.k9.util.PaginatedMessageFactory;
import com.tterrag.k9.util.Requirements;
import com.tterrag.k9.util.Requirements.RequiredType;

import sx.blah.discord.handle.obj.Permissions;

@Command
public class CommandSlap extends CommandPersisted<List<String>> {
    
    private static final Flag FLAG_ADD = new SimpleFlag('a', "add", "Adds a new slap.", true) {
        @Override
        public String longFormName() {
            return "add_slap";
        }
    };
    private static final Flag FLAG_LS = new SimpleFlag('l', "ls", "Lists all current slap strings.", false);
    
    private static final Argument<String> ARG_TARGET = new SentenceArgument("target", "The target of the slap.", true) {
        
        @Override
        public boolean required(Collection<Flag> flags) {
            return flags.isEmpty();
        }
    };
    
    private static final Requirements ADD_PERMS = Requirements.builder().with(Permissions.MANAGE_MESSAGES, RequiredType.ALL_OF).build();
    
    private static final int PER_PAGE = 10;

	private final List<String> options = Lists.newArrayList();
    private final Random rand = new Random();

    public CommandSlap() {
        super("slap", false, ArrayList::new);
        options.add("with a large trout!");
        options.add("with a big bat!");
        options.add("with a frying pan!");
        options.add("like a little bitch!");
    }
    
    @Override
    public TypeToken<List<String>> getDataType() {
        return new TypeToken<List<String>>(){};
    }
    
    @Override
    public void process(CommandContext ctx) throws CommandException {
        if (ctx.hasFlag(FLAG_LS)) {
            StringBuilder builder = new StringBuilder();
            PaginatedMessageFactory.Builder paginatedBuilder = PaginatedMessageFactory.INSTANCE.builder(ctx.getChannel());
            int i = 0;
            for (String suffix : storage.get(ctx.getMessage())) {
                if (i % PER_PAGE == 0) {
                    if (builder.length() > 0) {
                        paginatedBuilder.addPage(new BakedMessage(builder.toString(), null, false));
                    }
                    builder = new StringBuilder();
                    builder.append("List of custom slap suffixes (Page " + ((i / PER_PAGE) + 1) + "):\n");
                }
                builder.append(i++ + 1).append(") ").append(suffix).append("\n");
            }
            if (builder.length() > 0) {
                paginatedBuilder.addPage(new BakedMessage(builder.toString(), null, false));
            }
            paginatedBuilder.setParent(ctx.getMessage()).build().send();
            return;
        }

        if (ctx.hasFlag(FLAG_ADD)) {
            if (!ADD_PERMS.matches(ctx.getAuthor(), ctx.getGuild())) {
                throw new CommandException("You do not have permission to add slaps!");
            }
        	storage.get(ctx.getGuild()).add(ctx.getFlag(FLAG_ADD));
        	ctx.reply("Added new slap suffix.");
        	return;
        }
        
        String target = ctx.getArg(ARG_TARGET);
        boolean nou = target.equalsIgnoreCase(K9.instance.getOurUser().getName());
        String slapper = ctx.getMessage().getAuthor().getDisplayName(ctx.getGuild());
        StringBuilder builder = new StringBuilder(nou ? target : slapper);
        List<String> suffixes = Lists.newArrayList(options);
        suffixes.addAll(storage.get(ctx.getGuild()));
        
        builder.append(" slapped ").append(nou ? slapper : target).append(" " + suffixes.get(rand.nextInt(suffixes.size())));
        ctx.reply(builder.toString());
    }
    
    @Override
    public String getDescription() {
        return "For when someone just needs a good slap.";
    }
}
