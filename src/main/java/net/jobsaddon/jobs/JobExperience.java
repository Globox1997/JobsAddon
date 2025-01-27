package net.jobsaddon.jobs;

/*
 * id = job id
 * */
public class JobExperience {

    private final int id;
    private final int experience;

    public JobExperience(int id, int experience){
        this.id = id;
        this.experience = experience;
    }

    public int getId() {
        return id;
    }

    public int getExperience() {
        return experience;
    }
}
