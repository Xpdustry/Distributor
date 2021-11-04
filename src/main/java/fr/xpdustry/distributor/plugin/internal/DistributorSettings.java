package fr.xpdustry.distributor.plugin.internal;

import arc.files.*;

import org.aeonbits.owner.*;
import org.aeonbits.owner.Config.*;

import java.util.*;


@Sources("file:./config/distributor.properties")
public interface DistributorSettings extends Config, Accessible{
    @DefaultValue("./distributor")
    @Key("distributor.path")
    Fi getRootPath();

    default Fi getScriptsPath(){
        return getRootPath().child("scripts");
    }

    default Fi getLogsPath(){
        return getRootPath().child("logs");
    }

    @DefaultValue("init.js")
    @Key("distributor.scripts.init")
    String getInitScript();

    @DefaultValue("")
    @Key("distributor.scripts.startup")
    List<String> getStartupScripts();

    @DefaultValue("")
    @Key("distributor.scripts.shutdown")
    List<String> getShutdownScripts();

    @DefaultValue("10")
    @Key("distributor.scripts.max-runtime-duration")
    int getMaxRuntimeDuration();

    //TODO optimize this thing down here

    @DefaultValue("LOG")
    @Key("distributor.policy.runtime")
    RuntimePolicy getRuntimePolicy();

    enum RuntimePolicy{
        LOG, SILENT, THROW
    }
}
