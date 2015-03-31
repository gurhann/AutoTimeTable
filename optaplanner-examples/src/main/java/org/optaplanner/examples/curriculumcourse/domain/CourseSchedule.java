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

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

import com.thoughtworks.xstream.annotations.XStreamAlias;
import com.thoughtworks.xstream.annotations.XStreamConverter;
import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Inheritance;
import javax.persistence.InheritanceType;
import javax.persistence.JoinColumn;
import javax.persistence.JoinTable;
import javax.persistence.OneToMany;
import javax.persistence.Table;
import org.optaplanner.core.api.domain.solution.PlanningEntityCollectionProperty;
import org.optaplanner.core.api.domain.solution.PlanningSolution;
import org.optaplanner.core.api.domain.solution.Solution;
import org.optaplanner.core.api.domain.valuerange.ValueRangeProvider;
import org.optaplanner.core.api.score.buildin.hardsoft.HardSoftScore;
import org.optaplanner.core.impl.score.buildin.hardsoft.HardSoftScoreDefinition;
import org.optaplanner.examples.common.domain.AbstractPersistable;
import org.optaplanner.examples.curriculumcourse.domain.solver.CourseConflict;
import org.optaplanner.persistence.xstream.impl.score.XStreamScoreConverter;

@Entity(name = "CourseSchedule")
@Table(name = "CourseSchedule")
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@PlanningSolution
@XStreamAlias("CourseSchedule")
public class CourseSchedule extends AbstractPersistable implements Solution<HardSoftScore> {

    @Column(name = "name")
    private String name;

    @OneToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(name = "scheduleTeacherList",
            joinColumns = @JoinColumn(name = "schedule"),
            inverseJoinColumns = @JoinColumn(name = "teacher"))
    private List<Teacher> teacherList;
    
    @OneToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(name = "scheduleCurriculumList",
            joinColumns = @JoinColumn(name = "schedule"),
            inverseJoinColumns = @JoinColumn(name = "curricula"))
    private List<Curriculum> curriculumList;
    
    @OneToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(name = "scheduleCourseList",
            joinColumns = @JoinColumn(name = "schedule"),
            inverseJoinColumns = @JoinColumn(name = "course"))
    private List<Course> courseList;
    
    @OneToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(name = "scheduleDayList",
            joinColumns = @JoinColumn(name = "schedule"),
            inverseJoinColumns = @JoinColumn(name = "day"))
    private List<Day> dayList;
    
    @OneToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(name = "scheduleTimeslotList",
            joinColumns = @JoinColumn(name = "schedule"),
            inverseJoinColumns = @JoinColumn(name = "timeslot"))
    private List<Timeslot> timeslotList;
    
    @OneToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(name = "schedulePeriodList",
            joinColumns = @JoinColumn(name = "schedule"),
            inverseJoinColumns = @JoinColumn(name = "period"))
    private List<Period> periodList;
    
    @OneToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(name = "scheduleRoomLilst",
            joinColumns = @JoinColumn(name = "schedule"),
            inverseJoinColumns = @JoinColumn(name = "room"))
    private List<Room> roomList;

    
    @OneToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(name = "schedulePeriodPenaltyList",
            joinColumns = @JoinColumn(name = "schedule"),
            inverseJoinColumns = @JoinColumn(name = "periodPenalty"))
    private List<UnavailablePeriodPenalty> unavailablePeriodPenaltyList;

    @OneToMany(cascade = {CascadeType.PERSIST})
    @JoinTable(name = "scheduleLectureList",
            joinColumns = @JoinColumn(name = "schedule"),
            inverseJoinColumns = @JoinColumn(name = "lecture"))
    private List<Lecture> lectureList;
    
    @XStreamConverter(value = XStreamScoreConverter.class, types = {HardSoftScoreDefinition.class})
    private HardSoftScore score;

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Teacher> getTeacherList() {
        return teacherList;
    }

    public void setTeacherList(List<Teacher> teacherList) {
        this.teacherList = teacherList;
    }

    public List<Curriculum> getCurriculumList() {
        return curriculumList;
    }

    public void setCurriculumList(List<Curriculum> curriculumList) {
        this.curriculumList = curriculumList;
    }

    public List<Course> getCourseList() {
        return courseList;
    }

    public void setCourseList(List<Course> courseList) {
        this.courseList = courseList;
    }

    public List<Day> getDayList() {
        return dayList;
    }

    public void setDayList(List<Day> dayList) {
        this.dayList = dayList;
    }

    public List<Timeslot> getTimeslotList() {
        return timeslotList;
    }

    public void setTimeslotList(List<Timeslot> timeslotList) {
        this.timeslotList = timeslotList;
    }

    @ValueRangeProvider(id = "periodRange")
    public List<Period> getPeriodList() {
        return periodList;
    }

    public void setPeriodList(List<Period> periodList) {
        this.periodList = periodList;
    }

    @ValueRangeProvider(id = "roomRange")
    public List<Room> getRoomList() {
        return roomList;
    }

    public void setRoomList(List<Room> roomList) {
        this.roomList = roomList;
    }

    public List<UnavailablePeriodPenalty> getUnavailablePeriodPenaltyList() {
        return unavailablePeriodPenaltyList;
    }

    public void setUnavailablePeriodPenaltyList(List<UnavailablePeriodPenalty> unavailablePeriodPenaltyList) {
        this.unavailablePeriodPenaltyList = unavailablePeriodPenaltyList;
    }

    @PlanningEntityCollectionProperty
    public List<Lecture> getLectureList() {
        return lectureList;
    }

    public void setLectureList(List<Lecture> lectureList) {
        this.lectureList = lectureList;
    }

    public HardSoftScore getScore() {
        return score;
    }

    public void setScore(HardSoftScore score) {
        this.score = score;
    }

    // ************************************************************************
    // Complex methods
    // ************************************************************************

    public Collection<? extends Object> getProblemFacts() {
        List<Object> facts = new ArrayList<Object>();
        facts.addAll(teacherList);
        facts.addAll(curriculumList);
        facts.addAll(courseList);
        facts.addAll(dayList);
        facts.addAll(timeslotList);
        facts.addAll(periodList);
        facts.addAll(roomList);
        facts.addAll(unavailablePeriodPenaltyList);
        facts.addAll(precalculateCourseConflictList());
        // Do not add the planning entity's (lectureList) because that will be done automatically
        return facts;
    }

    private List<CourseConflict> precalculateCourseConflictList() {
        List<CourseConflict> courseConflictList = new ArrayList<CourseConflict>();
        for (Course leftCourse : courseList) {
            for (Course rightCourse : courseList) {
                if (leftCourse.getId() < rightCourse.getId()) {
                    int conflictCount = 0;
                    if (leftCourse.getTeacher().equals(rightCourse.getTeacher())) {
                        conflictCount++;
                    }
                    for (Curriculum curriculum : leftCourse.getCurriculumList()) {
                        if (rightCourse.getCurriculumList().contains(curriculum)) {
                            conflictCount++;
                        }
                    }
                    if (conflictCount > 0) {
                        courseConflictList.add(new CourseConflict(leftCourse, rightCourse, conflictCount));
                    }
                }
            }
        }
        return courseConflictList;
    }

}
