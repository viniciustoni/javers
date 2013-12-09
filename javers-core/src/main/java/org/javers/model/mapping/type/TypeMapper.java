package org.javers.model.mapping.type;

import org.javers.common.reflection.ReflectionUtil;
import org.javers.common.validation.Validate;
import org.javers.core.exceptions.JaversException;
import org.javers.core.exceptions.JaversExceptionCode;

import java.lang.reflect.ParameterizedType;
import java.lang.reflect.Type;
import java.util.*;

import static org.javers.common.reflection.ReflectionUtil.extractClass;
import static org.javers.common.validation.Validate.argumentIsNotNull;

/**
 * Maps Java types into Javers types
 *
 * @author bartosz walacik
 */
public class TypeMapper {
    private Map<Type, JaversType> mappedTypes;

    public TypeMapper() {
        mappedTypes = new HashMap<>();

        //primitives
        registerPrimitiveType(Integer.TYPE);
        registerPrimitiveType(Boolean.TYPE);
        registerPrimitiveType(Double.TYPE);
        registerPrimitiveType(Float.TYPE);
        registerPrimitiveType(Long.TYPE);

        //primitive boxes
        registerPrimitiveType(Integer.class);
        registerPrimitiveType(Boolean.class);
        registerPrimitiveType(Double.class);
        registerPrimitiveType(Float.class);
        registerPrimitiveType(String.class);
        registerPrimitiveType(Enum.class);
        registerPrimitiveType(Long.class);

        //array
        addType(new ArrayType());

        //Collections
        addType(new CollectionType(Set.class));
        addType(new CollectionType(List.class));
    }

    /**
     * @throws JaversExceptionCode TYPE_NOT_MAPPED
     */
    public JaversType getJavesrType(Type javaType) {
        argumentIsNotNull(javaType);

        JaversType jType = getMatchingJaversType(javaType);
        if (jType != null) {
            return jType;
        }

        //try to spawn generic from prototype
        if (javaType instanceof  ParameterizedType) {
            JaversType spawned = spawnGenericFromPrototype((ParameterizedType)javaType);
            addType(spawned);
            return spawned;
        }

        throw new JaversException(JaversExceptionCode.TYPE_NOT_MAPPED, javaType);
    }

    /**
     * @throws JaversExceptionCode TYPE_NOT_MAPPED
     */
    private JaversType spawnGenericFromPrototype(ParameterizedType javaType) {
        JaversType prototype = findNoGenericJaversTypeAssignableFrom(javaType);

        return null;
    }

    /**
     * @return null if not found
     */
    private JaversType getMatchingJaversType(Type javaType) {
        return mappedTypes.get(javaType);
    }

    /**
     * @throws JaversExceptionCode TYPE_NOT_MAPPED
     */
    private JaversType findNoGenericJaversTypeAssignableFrom(Type javaType) {
        argumentIsNotNull(javaType);

        for (JaversType javersType : mappedTypes.values()){
            if(!javersType.isGenericType() && javersType.isAssignableFrom(extractClass(javaType))){
                return javersType;
            }
        }

        throw new JaversException(JaversExceptionCode.TYPE_NOT_MAPPED, javaType);
    }

    private boolean isMapped(Type javaType) {
        return mappedTypes.containsKey(javaType);
    }

    public int getCountOfEntitiesAndValueObjects() {
        return getMappedEntityReferenceTypes().size() +
               getMappedValueObjectTypes().size();
    }

    private void addType(JaversType jType) {
        mappedTypes.put(jType.getBaseJavaType(), jType);
    }

    public void registerCollectionType(Type collectionType) {
        addType(new CollectionType(collectionType));
    }

    public void registerPrimitiveType(Class<?> primitiveClass) {
        addType(new PrimitiveType(primitiveClass));
    }

    public void registerEntityReferenceType(Class<?> entityClass) {
        addType(new EntityReferenceType(entityClass));
    }

    public void registerValueObjectType(Class<?> objectValue) {
        addType(new ValueObjectType(objectValue));

    }

    public <T extends JaversType> List<T> getMappedTypes(Class<T> ofType) {
        List<T> result = new ArrayList<>();
        for(JaversType jType : mappedTypes.values()) {
            if(ofType.isAssignableFrom(jType.getClass()) ) {
                result.add((T)jType);
            }
        }
        return result;
    }

    public List<ValueObjectType> getMappedValueObjectTypes() {
        return getMappedTypes(ValueObjectType.class);
    }

    public List<EntityReferenceType> getMappedEntityReferenceTypes() {
        return getMappedTypes(EntityReferenceType.class);
    }

    /*
    public List<Class> getReferenceTypes() {
        List<Class> referenceClasses = new ArrayList<>();
        for(JaversType entry : mappedTypes) {
            if(entry.isReferencedType()) {
                referenceClasses.add((Class)entry.getBaseJavaType());
            }
        }
        return referenceClasses;
    }*/
}
