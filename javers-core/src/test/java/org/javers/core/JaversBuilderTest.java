package org.javers.core;


import org.junit.Test;

import javax.persistence.Id;

import static org.javers.core.JaversBuilder.javers;
import static org.javers.test.assertion.Assertions.assertThat;

/**
 * @author bartosz walacik
 */
public class JaversBuilderTest {

    @Test
    public void shouldBootNonCoreModule() {
        //given
        JaversBuilder javersBuilder = javers();

        //when
        javersBuilder.addModule(new DummyJaversModule()).build();

        //then
        assertThat(javersBuilder.getContainer().getComponent(DummyJaversBean.class)).isNotNull();
    }

    @Test
    public void shouldManageEntity() {
        //when
        Javers javers = javers().registerEntity(DummyEntity.class).build();

        //then
        assertThat(javers.isManaged(DummyEntity.class)).isTrue();
    }

    @Test
    public void shouldInitializeEntityManager() {
        //given
        JaversBuilder javersBuilder = javers().registerEntity(DummyEntity.class)
                                              .registerValueObject(DummyNetworkAddress.class);

        //when
        javersBuilder.build();

        //then
        EntityManager em = getEntityManager(javersBuilder);
        TypeMapper tm    = getTypeMapper(javersBuilder);
        assertThat(em.isInitialized()).isTrue();
        assertThat(tm.getCountOfEntitiesAndValueObjects()).isEqualTo(2);
    }

    private EntityManager getEntityManager(JaversBuilder javersBuilder) {
        return (EntityManager)javersBuilder.getContainer().getComponent(EntityManager.class);
    }

    private TypeMapper getTypeMapper(JaversBuilder javersBuilder) {
        return (TypeMapper)javersBuilder.getContainer().getComponent(TypeMapper.class);
    }

    @Test
    public void shouldManageValueObject() {
        //when
        Javers javers = javers().registerValueObject(DummyNetworkAddress.class).build();

        //then
        assertThat(javers.isManaged(DummyNetworkAddress.class)).isTrue();
    }

    @Test
    public void shouldCreateJavers() throws Exception {
        //when
        Javers javers = javers().build();

        //then
        assertThat(javers).isNotNull();
    }

    @Test
    public void shouldCreateMultipleJaversInstances() {
        //when
        Javers javers1 = javers().build();
        Javers javers2 = javers().build();

        //then
        assertThat(javers1).isNotSameAs(javers2);
    }

    private class DummyEntity {
        private int id;
        private DummyEntity parent;

        @Id
        private int getId() {
            return id;
        }

        private DummyEntity getParent() {
            return parent;
        }
    }
}