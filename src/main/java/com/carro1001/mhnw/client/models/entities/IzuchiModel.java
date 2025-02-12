package com.carro1001.mhnw.client.models.entities;

import com.carro1001.mhnw.entities.IzuchiEntity;
import net.minecraft.resources.ResourceLocation;
import software.bernie.geckolib3.model.AnimatedTickingGeoModel;

import static com.carro1001.mhnw.utils.MHNWReferences.IZUCHI;
import static com.carro1001.mhnw.utils.MHNWReferences.MODID;

public class IzuchiModel extends AnimatedTickingGeoModel<IzuchiEntity> {
    @Override
    public ResourceLocation getModelResource(IzuchiEntity object)
    {
        return new ResourceLocation(MODID, "geo/"+ IZUCHI +".geo.json");
    }

    @Override
    public ResourceLocation getTextureResource(IzuchiEntity object)
    {
        return new ResourceLocation(MODID, "textures/entity/"+ IZUCHI +".png");
    }

    @Override
    public ResourceLocation getAnimationResource(IzuchiEntity object)
    {
        return new ResourceLocation(MODID, "animations/"+ IZUCHI +".animation.json");
    }

}
