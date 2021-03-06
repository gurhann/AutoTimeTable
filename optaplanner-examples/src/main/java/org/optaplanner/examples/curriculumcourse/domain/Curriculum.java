/*
 * Copyright 2010 JBoss Inc
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

package org.optaplanner.examples.curriculumcourse.domain;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.NamedQueries;
import javax.persistence.NamedQuery;
import javax.persistence.Table;
import org.optaplanner.examples.common.domain.AbstractPersistable;

@Entity(name = "Curriculum")
@Table(name = "Curriculum")
@NamedQueries({
    @NamedQuery(name = "Curriculum.findByCode",
            query = "SELECT c FROM Curriculum c WHERE c.code=:code"),
    @NamedQuery(name = "Curriculum.findAll",
            query = "SELECT c FROM Curriculum c")
})
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@XStreamAlias("Curriculum")
public class Curriculum extends AbstractPersistable {

    @Column(name = "code")
    private String code;
    @Column(name = "nightClass")
    private boolean nightClass;
    
    public Curriculum() {
        
    }
    
    public Curriculum(String code, boolean nightClass) {
        this.code = code;
        this.nightClass = nightClass;
    }
    public String getCode() {
        return code;
    }

    public void setCode(String code) {
        this.code = code;
    }

    public String getLabel() {
        return code;
    }

    public boolean isNightClass() {
        return nightClass;
    }

    public void setNightClass(boolean nightClass) {
        this.nightClass = nightClass;
    }
    

    @Override
    public String toString() {
        return code;
    }
}
