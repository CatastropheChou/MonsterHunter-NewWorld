package com.carro1001.mhnw.client.models.entities;

import com.carro1001.mhnw.entities.RathianEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedGeoModel;

import static com.carro1001.mhnw.utils.MHNWReferences.MODID;

public class RathianModel extends AnimatedGeoModel<RathianEntity> {
    public RathianModel() {

    }


    @Override
    public ResourceLocation getModelResource(RathianEntity object)
    {
        return new ResourceLocation(MODID, "geo/rathian.geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(RathianEntity object)
    {
        return new ResourceLocation(MODID, "textures/entity/rathian.png");
    }

    @Override
    public ResourceLocation getAnimationResource(RathianEntity object)
    {
        return new ResourceLocation(MODID, "animations/rathian.animation.json");
    }


}
