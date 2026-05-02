package com.mchelinaval.util;

import com.mchelinaval.McHeliNavalAddon;
import net.minecraft.entity.Entity;

import java.lang.reflect.Field;
import java.util.HashMap;
import java.util.Map;

/**
 * MCHELIの内部フィールドにリフレクションでアクセスするユーティリティ。
 * フィールドは初回アクセス時にキャッシュして毎回取得するコストを減らす。
 */
public class McheliReflect {

    // フィールドキャッシュ（クラス名+フィールド名 → Fieldオブジェクト）
    private static final Map<String, Field> FIELD_CACHE = new HashMap<>();

    // MCH_EntityAircraftのクラス名
    public static final String ENTITY_AIRCRAFT_CLASS = "mcheli.aircraft.MCH_EntityAircraft";

    // -------------------------------------------------------
    // MCHELIの機体かどうかを判定する
    // -------------------------------------------------------
    public static boolean isMcheliAircraft(Entity entity) {
        if (entity == null) return false;
        try {
            Class<?> aircraftClass = Class.forName(ENTITY_AIRCRAFT_CLASS);
            return aircraftClass.isInstance(entity);
        } catch (Exception e) {
            return false;
        }
    }

    // -------------------------------------------------------
    // 速度を取得する
    // -------------------------------------------------------
    public static double getVelocityX(Entity entity) {
        return getDouble(entity, "velocityX");
    }

    public static double getVelocityY(Entity entity) {
        return getDouble(entity, "velocityY");
    }

    public static double getVelocityZ(Entity entity) {
        return getDouble(entity, "velocityZ");
    }

    // -------------------------------------------------------
    // 速度を設定する（カタパルトの核心処理）
    // -------------------------------------------------------
    public static void setVelocityX(Entity entity, double value) {
        setDouble(entity, "velocityX", value);
    }

    public static void setVelocityY(Entity entity, double value) {
        setDouble(entity, "velocityY", value);
    }

    public static void setVelocityZ(Entity entity, double value) {
        setDouble(entity, "velocityZ", value);
    }

    // -------------------------------------------------------
    // 内部ヘルパー：フィールドをキャッシュしてdoubleを取得
    // -------------------------------------------------------
    private static double getDouble(Entity entity, String fieldName) {
        if (entity == null) return 0.0;
        try {
            Field f = getField(entity.getClass(), fieldName);
            if (f == null) return 0.0;
            return f.getDouble(entity);
        } catch (Exception e) {
            McHeliNavalAddon.logger.warn("McheliReflect: getDouble({}) failed: {}", fieldName, e.getMessage());
            return 0.0;
        }
    }

    private static void setDouble(Entity entity, String fieldName, double value) {
        if (entity == null) return;
        try {
            Field f = getField(entity.getClass(), fieldName);
            if (f == null) return;
            f.setDouble(entity, value);
        } catch (Exception e) {
            McHeliNavalAddon.logger.warn("McheliReflect: setDouble({}) failed: {}", fieldName, e.getMessage());
        }
    }

    /**
     * クラス階層を辿ってフィールドを探し、キャッシュして返す。
     * 見つからなければnullを返す。
     */
    private static Field getField(Class<?> clazz, String fieldName) {
        String key = clazz.getName() + "#" + fieldName;
        if (FIELD_CACHE.containsKey(key)) {
            return FIELD_CACHE.get(key);
        }

        // クラス階層を上に辿りながら探す
        Class<?> current = clazz;
        while (current != null) {
            try {
                Field f = current.getDeclaredField(fieldName);
                f.setAccessible(true); // protectedでもアクセス可能にする
                FIELD_CACHE.put(key, f);
                return f;
            } catch (NoSuchFieldException e) {
                current = current.getSuperclass(); // 親クラスへ
            }
        }

        McHeliNavalAddon.logger.warn("McheliReflect: field '{}' not found in {}", fieldName, clazz.getName());
        FIELD_CACHE.put(key, null); // 見つからなかった結果もキャッシュ
        return null;
    }
}
