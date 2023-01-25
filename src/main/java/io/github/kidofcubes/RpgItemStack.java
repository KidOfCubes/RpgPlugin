package io.github.kidofcubes;

import org.bukkit.craftbukkit.v1_19_R2.persistence.CraftPersistentDataContainer;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

public class RpgItemStack implements RpgItem{
    public static RpgItem getRpgItemInstance(ItemMeta itemMeta, ItemStack thing){
        if(itemMeta==null){
            itemMeta=thing.getItemMeta();
            thing.setItemMeta(itemMeta);
        }
        CraftPersistentDataContainer persistentDataContainer = (CraftPersistentDataContainer) itemMeta.getPersistentDataContainer();
        if(((RpgObjectHolder)persistentDataContainer).getObject()==null){
            if(persistentDataContainer.getRaw().containsKey(RpgObject.metadataKey.toString())){ //contains data, init rpgitem from data
                RpgObjectTag tag = (RpgObjectTag)(persistentDataContainer).getRaw().get(RpgObject.metadataKey.toString());
                ((RpgObjectHolder) itemMeta.getPersistentDataContainer()).setObject(new RpgItemStack(thing).loadFromJson(tag.getLoadedText()));
            }else{ //no previous data, init new rpgitem

                ((RpgObjectHolder)itemMeta.getPersistentDataContainer()).setObject(new RpgItemStack(thing));
                ((CraftPersistentDataContainer)itemMeta.getPersistentDataContainer()).getRaw().put(RpgObject.metadataKey.toString(),new RpgObjectTag(((RpgObjectHolder)itemMeta.getPersistentDataContainer()).getObject()));
            }
        }
        return (RpgItem) ((RpgObjectHolder)itemMeta.getPersistentDataContainer()).getObject(); //were just assuming
    }

    private final ItemStack itemStack;
    private final UUID uuid;
    public RpgItemStack(ItemStack itemStack){
        this.itemStack=itemStack;
        uuid=UUID.randomUUID();
    }

    private int level=0;
    private double mana=0;

    Map<String,RpgClass> rpgClasses = new HashMap<>();

    Map<String,Stat> stats = new HashMap<>();

//    private List<Stat,>

    @Override
    public String getName() {
        return "RPGOBJECT";
    }
    @Override
    public boolean isLoaded() {
        return true;
    }

    @Override
    public UUID getUUID() {
        return uuid;
    }

    @Override
    public int getLevel() {
        return level;
    }

    @Override
    public void setLevel(int level) {
        this.level=level;
    }

    @Override
    public double getMana() {
        return mana;
    }

    @Override
    public void setMana(double mana) {
        this.mana=mana;
    }

    @Override
    public List<RpgClass> getRpgClasses() {
        return rpgClasses.values().stream().toList();
    }

    @Override
    public void addRpgClass(RpgClass rpgClass) {
        rpgClasses.put(rpgClass.getFullName(),rpgClass);
    }

    @Override
    public RpgClass getRpgClass(String rpgClass) {
        return rpgClasses.get(rpgClass);
    }

    @Override
    public void removeRpgClass(String rpgClass) {
        rpgClasses.remove(rpgClass);
    }

    @Override
    public boolean hasRpgClass(String rpgClass) {
        return rpgClasses.containsKey(rpgClass);
    }

    @Override
    public void use(RpgObject rpgObject) {

    }

    @Override
    public void stopUsing(RpgObject rpgObject) {

    }

    @Override
    public boolean usedBy(RpgObject rpgObject) {
        return false;
    }

    @Nullable
    @Override
    public RpgObject getParent() {
        return null;
    }

    @Override
    public void setParent(RpgObject parent) {

    }



    @Override
    public void addStat(Stat stat, boolean force) {
        stats.put(stat.getName(),stat);
    }

    @Override
    public boolean hasStat(String stat) {
        return stats.containsKey(stat);
    }

    @Nullable
    @Override
    public Stat getStat(String stat) {
        return stats.get(stat);
    }

    @Override
    public List<Stat> getStats() {
        return stats.values().stream().toList();
    }

    @Override
    public void removeStat(String stat) {
        stats.remove(stat);
    }

    @Override
    public @NotNull Map<Class<? extends Stat>, List<Stat>> getUsedStats() {
        return Map.of();
    }
}