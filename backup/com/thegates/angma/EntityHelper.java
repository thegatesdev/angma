package com.thegates.angma;

import net.minecraft.entity.Entity;
import net.minecraft.entity.EntityType;
import net.minecraft.util.Identifier;

public class EntityHelper {


    public static Identifier getMobId(Entity entity){
        return EntityType.getId(entity.getType());
    }
}
