/*
 * Copyright 2015 JBoss by Red Hat.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.optaplanner.curriculumcourse.dao;

import java.util.List;
import javax.persistence.EntityManager;
import javax.persistence.Query;
import org.optaplanner.curriculumcourse.exception.NoSuchRoomException;
import org.optaplanner.examples.curriculumcourse.domain.Room;

/**
 *
 * @author gurhan
 */
public class RoomDao extends GenericDaoImp<Room> {

    public RoomDao(EntityManager em) {
        super(em, Room.class);
    }

    public List<Room> findAll() {
        Query query = em.createNamedQuery("Room.findAll");
        return query.getResultList();
    }

    public Room findRoom(String code) throws NoSuchRoomException {
        Query query = em.createNamedQuery("Room.findByCode");
        query.setParameter("code", code);
        try {
            return (Room) query.getSingleResult();
        }catch (Exception e) {
            throw new NoSuchRoomException();
        }
    }
}
