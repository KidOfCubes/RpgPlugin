package io.github.KidOfCubes;

import io.github.KidOfCubes.Events.*;
import io.github.KidOfCubes.Managers.RpgManager;
import io.github.KidOfCubes.Types.DamageType;
import io.github.KidOfCubes.Types.EntityRelation;
import org.bukkit.entity.LivingEntity;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.*;

import static io.github.KidOfCubes.ExtraFunctions.isEmpty;
import static io.github.KidOfCubes.RpgPlugin.key;
import static io.github.KidOfCubes.RpgPlugin.logger;

public class RpgEntity extends RpgObject {
    public LivingEntity livingEntity;
    private double health;
    private double maxHealth;
    private final Map<EntityRelation,List<UUID>> relations = new HashMap<>();

    public RpgEntity(LivingEntity livingEntity){
        this.livingEntity = livingEntity;
        if(livingEntity.getPersistentDataContainer().has(key)){
            loadFromJson(livingEntity.getPersistentDataContainer().get(key, PersistentDataType.STRING));
        }
        setUUID(livingEntity.getUniqueId());
        for (EntityRelation relation :
                EntityRelation.values()) {
            relations.put(relation,new ArrayList<>());
        }
        level = 0;
    }
    public RpgEntity(LivingEntity livingEntity, boolean tempEntity){
        this(livingEntity);
        temporary = tempEntity;
    }
    public RpgEntity(LivingEntity livingEntity, RpgEntity parent, boolean tempEntity){
        this(livingEntity,tempEntity);
        setParent(parent);
    }


    public void damage(DamageType type, double amount){ //AMOUNT IS FOR BASE AMOUNT
        RpgEntityDamageEvent event = new RpgEntityDamageEvent(this, type,amount);
        event.callEvent();
        livingEntity.damage(event.getTotalDamage());
    }

    public RpgEntityDamageEvent damage(DamageType type, double amount, RpgObject attacker){ //AMOUNT IS FOR BASE AMOUNT, WILL RUN STATS i think
        RpgEntityDamageEvent event = new RpgEntityDamageByObjectEvent(this,type,amount, attacker);
        event.callEvent();
        livingEntity.damage(event.getTotalDamage());
        return event;
    }
    public RpgEntityHealEvent heal(double amount){
        RpgEntityHealEvent event = new RpgEntityHealEvent(this, amount);
        event.callEvent();
        livingEntity.setHealth(livingEntity.getHealth()+event.getAmount());
        return event;
    }
    public RpgEntityHealByObjectEvent heal(double amount, @NotNull RpgObject healer){

        RpgEntityHealByObjectEvent event = new RpgEntityHealByObjectEvent(this, amount, healer);
        event.callEvent();
        livingEntity.setHealth(livingEntity.getHealth()+event.getAmount());
        return event;
    }


    private void cleanRelations(){
        for (Map.Entry<EntityRelation,List<UUID>> entry : relations.entrySet()) {
            List<UUID> list = entry.getValue();
            for (int i = list.size()-1; i > -1; i--) {
                if(!RpgManager.checkEntityExists(list.get(i))) list.remove(i);
            }
        }
    }

    public void setRelation(UUID uuid, EntityRelation relation){
        for (Map.Entry<EntityRelation,List<UUID>> entry : relations.entrySet()) {
            entry.getValue().remove(uuid);
        }
        if(relation!=EntityRelation.Neutral){
            relations.get(relation).add(uuid);
        }
    }
    public EntityRelation getRelation(UUID uuid){
        cleanRelations();
        for (Map.Entry<EntityRelation,List<UUID>> entry : relations.entrySet()) {
            if(entry.getValue().contains(uuid)){
                return entry.getKey();
            }
        }
        return EntityRelation.Neutral;
    }

    @Nullable
    public UUID currentTarget(){
        if(relations.get(EntityRelation.Enemy).size()!=0){
            cleanRelations();
            if(relations.get(EntityRelation.Enemy).size()!=0) {
                return relations.get(EntityRelation.Enemy).get(relations.get(EntityRelation.Enemy).size() - 1);
            }else{
                return null;
            }
        }else{
            return null;
        }
    }

    Map<String,Integer> effectiveStatsCache = new HashMap<>();
    long effectiveStatsLastUpdate = 0;
    @Override
    public Map<String,Integer> getEffectiveStats(){
        long now = System.currentTimeMillis();
        if(now-effectiveStatsLastUpdate>250) {
            effectiveStatsLastUpdate = now;

            effectiveStatsCache = new HashMap<>(getStats());
            if (livingEntity.getEquipment() != null) {
                for (EquipmentSlot slot : EquipmentSlot.values()) {
                    ItemStack item = livingEntity.getEquipment().getItem(slot);
                    if (!isEmpty(item)) {
                        //logger.info("length of effective stats is "+temp.getEffectiveStats().size());
                        effectiveStatsCache.putAll(RpgManager.getItem(item).getEffectiveStats());
                    }
                }
            }
        }
        return effectiveStatsCache;

    }

    public boolean exists() {
        return livingEntity.isValid()&&!livingEntity.isDead()&&livingEntity.getHealth()>0;
    }

    @Override
    public void save() {
        livingEntity.getPersistentDataContainer().set(key, PersistentDataType.STRING,toJson());
    }
}
