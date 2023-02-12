package io.github.kidofcubes.rpgplugin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.nbt.Tag;
import org.bukkit.NamespacedKey;
import org.jetbrains.annotations.Nullable;

import java.io.DataOutput;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

public class RpgObjectTag extends CompoundTag {

    public static final NamespacedKey RpgObjectTagKey = new NamespacedKey("rpg_plugin", "rpg_object");
    public static final String dataKey = ("data");
    public static final String typeKey = ("type");

    public RpgObjectTag(RpgObject rpgObject){
        super();
        setObject(rpgObject);
        if(rpgObject.getRpgType()!=RpgObject.defaultTypeKey){
            setSavedType(rpgObject.getRpgType());
        }
    }
    private RpgObjectTag(){
        super();
    }
    DynamicallySavedTag<RpgObject> rpgDataTag;

    @Nullable
    public RpgObject getObject(){
        if(rpgDataTag==null){
            return null;
        }
        return rpgDataTag.getObject();
    }

    public void setObject(RpgObject object){
        if(rpgDataTag==null){
            rpgDataTag = new DynamicallySavedTag<RpgObject>(object);
            put(dataKey,rpgDataTag);
        }
        rpgDataTag.setObject(object);
    }

    public void unload(){
        if(contains(dataKey)){
            ((DynamicallySavedTag<?>) Objects.requireNonNull(get(dataKey))).unload();
        }
    }


    public String getJson(){
        if(!contains(dataKey)){
            return "";
        }
        return ((DynamicallySavedTag<?>)(Objects.requireNonNull(get(dataKey)))).getJsonData();
    }

    public void setSavedType(NamespacedKey key){
        putString(typeKey,key.asString());
    }
    public NamespacedKey getSavedType(){
        if(contains(typeKey)){
            return NamespacedKey.fromString(getString(typeKey));
        }
        return RpgObject.defaultTypeKey;
    }

    public static RpgObjectTag fromCompoundTag(@Nullable CompoundTag compoundTag){
        System.out.println("CONSTRUCTING A NEW RPGOBJECT TAG FROM "+compoundTag);
        if(compoundTag instanceof RpgObjectTag rpgObjectTag){
            return rpgObjectTag;
        }
        if(compoundTag!=null) {
            RpgObjectTag instance = new RpgObjectTag();
            if (compoundTag.contains(typeKey)) {
                instance.putString(typeKey, compoundTag.getString(typeKey));
            }
            if (compoundTag.contains(dataKey)) {
                if(compoundTag.get(dataKey) instanceof DynamicallySavedTag<?> dynamicallySavedTag) {
                    instance.put(dataKey, dynamicallySavedTag);
                }else {
                    instance.put(dataKey, new DynamicallySavedTag<RpgObject>(compoundTag.getByteArray(dataKey)));
                }
            }
            return instance;
        }
        return new RpgObjectTag();
    }

    @Override
    public void write(DataOutput output) throws IOException {
        for(String string : this.tags.keySet()) {
            Tag tag = this.tags.get(string);
            writeNamedTag(string, tag, output);
        }

        output.writeByte(0);
    }

    private static void writeNamedTag(String key, Tag element, DataOutput output) throws IOException {
        output.writeByte(element.getId());
        if (element.getId() != 0) {
            output.writeUTF(key);
            element.write(output);
        }
    }

    @Override
    public CompoundTag copy() {
        return super.copy();
    }
}
