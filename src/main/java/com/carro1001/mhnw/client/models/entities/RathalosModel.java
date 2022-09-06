package com.carro1001.mhnw.client.models.entities;

import com.carro1001.mhnw.entities.RathalosEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import static com.carro1001.mhnw.utils.MHNWReferences.MODID;

public class RathalosModel extends AnimatedGeoModel<RathalosEntity> {

    @Override
    public ResourceLocation getModelResource(RathalosEntity object)
    {
        return new ResourceLocation(MODID, "geo/rathalos.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RathalosEntity object)
    {
        return new ResourceLocation(MODID, "textures/entity/rathalos.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RathalosEntity object)
    {
        return new ResourceLocation(MODID, "animations/rathalos.animation.json");
    }

}
