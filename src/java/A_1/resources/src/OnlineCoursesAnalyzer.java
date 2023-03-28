package A_1.resources.src;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;
import org.junit.jupiter.params.shadow.com.univocity.parsers.annotations.Copy;

public class OnlineCoursesAnalyzer {



    List<Course> courses = new ArrayList<>();

    public OnlineCoursesAnalyzer(String datasetPath) {
        BufferedReader br = null;
        String line;
        try {
            br = new BufferedReader(new FileReader(datasetPath, StandardCharsets.UTF_8));
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] info = line.split(",(?=([^\\\"]*\\\"[^\\\"]*\\\")*[^\\\"]*$)", -1);
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4], info[5],
                        Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                        Integer.parseInt(info[9]), Integer.parseInt(info[10]), Double.parseDouble(info[11]),
                        Double.parseDouble(info[12]), Double.parseDouble(info[13]), Double.parseDouble(info[14]),
                        Double.parseDouble(info[15]), Double.parseDouble(info[16]), Double.parseDouble(info[17]),
                        Double.parseDouble(info[18]), Double.parseDouble(info[19]), Double.parseDouble(info[20]),
                        Double.parseDouble(info[21]), Double.parseDouble(info[22]));
                courses.add(course);
            }
        } catch (IOException e) {
            e.printStackTrace();
        } finally {
            if (br != null) {
                try {
                    br.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }

    //1
    public Map<String, Integer> getPtcpCountByInst() {
            Map<String, Integer> countByInst = new TreeMap<>();
            for (Course course : courses) {
                String institution = course.institution;
                int participants = course.participants;
                countByInst.put(institution, countByInst.getOrDefault(institution, 0) + participants);
            }
            return countByInst;
    }

    //2


    public Map<String, Integer> getPtcpCountByInstAndSubject() {
        Map<String, Integer> result = new HashMap<>();
        for (Course course : courses) {
            String key = course.getInstitution() + "-" + course.getSubject();
            Collection<Student> participants = course.getParticipants();
            if (result.containsKey(key)) {
                result.put(key, result.get(key) + participants.size());
            } else {
                result.put(key, participants.size());
            }
        }
        return result.entrySet().stream()
            .sorted((e1, e2) -> {
                int cmp = e2.getValue().compareTo(e1.getValue());
                return cmp != 0 ? cmp : e1.getKey().compareTo(e2.getKey());
            })
            .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue, (v1, v2) -> v1, LinkedHashMap::new));
    }



    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> result = new HashMap<>();
        Map<String, Set<String>> independentCoursesByInstructor = new HashMap<>();
        Map<String, Set<String>> coDevelopedCoursesByInstructor = new HashMap<>();

        for (Course course : courses) {
            String[] instructorNames = course.getInstructors().split(",");
            for (String instructorName : instructorNames) {
                if (instructorName.startsWith("\"")) instructorName = instructorName.substring(1);
                if (instructorName.endsWith("\"")) instructorName = instructorName.substring(0, instructorName.length() - 1);

                String courseTitle = course.getTitle();
                if (courseTitle.startsWith("\"")) courseTitle = courseTitle.substring(1);
                if (courseTitle.endsWith("\"")) courseTitle = courseTitle.substring(0, courseTitle.length() - 1);

                if (!independentCoursesByInstructor.containsKey(instructorName)) {
                    independentCoursesByInstructor.put(instructorName, new TreeSet<>());
                }
                if (!coDevelopedCoursesByInstructor.containsKey(instructorName)) {
                    coDevelopedCoursesByInstructor.put(instructorName, new TreeSet<>());
                }

                if (instructorNames.length == 1) {
                    independentCoursesByInstructor.get(instructorName).add(courseTitle);
                } else {
                    coDevelopedCoursesByInstructor.get(instructorName).add(courseTitle);
                }
            }
        }

        for (String instructorName : independentCoursesByInstructor.keySet()) {
            List<List<String>> coursesByType = new ArrayList<>();
            coursesByType.add(new ArrayList<>(independentCoursesByInstructor.getOrDefault(instructorName, Collections.emptySet())));
            coursesByType.add(new ArrayList<>(coDevelopedCoursesByInstructor.getOrDefault(instructorName, Collections.emptySet())));
            result.put(instructorName, coursesByType);
        }

        return result;
    }
    //4
    public List<String> getCourses(int topK, String by) {
        List<Course> sortedCourses;
        if (by.equals("hours")) {
            sortedCourses = courses.stream()
                .sorted(Comparator.comparingDouble(Course::getTotalHours).reversed()
                    .thenComparing(Course::getTitle))
                .collect(Collectors.toList());
        } else if (by.equals("participants")) {
            sortedCourses = courses.stream()
                .sorted(Comparator.comparingInt(Course::getParticipants).reversed()
                    .thenComparing(Course::getTitle))
                .collect(Collectors.toList());
        } else {
            throw new IllegalArgumentException("Invalid sort criteria: " + by);
        }

        List<String> result = new ArrayList<>();
        Set<String> addedTitles = new HashSet<>();
        for (Course course : sortedCourses) {
            if (result.size() >= topK) {
                break;
            }
            String title = course.getTitle();
            if (addedTitles.contains(title)) {
                continue;
            }
            result.add(title);
            addedTitles.add(title);
        }

        return result;
    }
    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<Course> matchingCourses = courses.stream()
            .filter(course -> course.getSubject().toLowerCase().contains(courseSubject.toLowerCase()))
            .filter(course -> course.getPercentAudited() >= percentAudited)
            .filter(course -> course.getTotalHours() <= totalCourseHours)
            .sorted(Comparator.comparing(Course::getTitle))
            .collect(Collectors.toList());

        List<String> result = new ArrayList<>();
        Set<String> addedTitles = new HashSet<>();
        for (Course course : matchingCourses) {
            String title = course.getTitle();
            if (addedTitles.contains(title)) {
                continue;
            }
            result.add(title);
            addedTitles.add(title);
        }

        return result;
    }

    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        Map<String, List<Double>> courseStats = new HashMap<>();
        for (Course course : courses) {
            String courseNum = course.getNumber();
            List<Double> stats = new ArrayList<>();
            stats.add(course.getMedianAge());
            stats.add(course.getPercentMale());
            stats.add(course.getPercentDegree());
            courseStats.put(courseNum, stats);
        }

        List<Map.Entry<String, Double>> similarityList = new ArrayList<>();
        for (Course course : courses) {
            String courseNum = course.getNumber();
            List<Double> stats = courseStats.get(courseNum);
            double similarity = Math.pow(age - stats.get(0), 2) + Math.pow(gender * 100 - stats.get(1), 2)
                + Math.pow(isBachelorOrHigher * 100 - stats.get(2), 2);
            similarityList.add(new AbstractMap.SimpleEntry<>(course.getTitle(), similarity));
        }

        List<String> result = similarityList.stream()
            .sorted(Comparator.comparing(t -> Map.Entry.getValue(t)).thenComparing(Map.Entry::getKey))
            .map(Map.Entry::getKey)
            .distinct()
            .collect(Collectors.toList());

        return result.subList(0, Math.min(10, result.size()));
    }

}

class Course {


    public String getInstitution() {
        return institution;
    }

    public String getNumber() {
        return number;
    }

    public Date getLaunchDate() {
        return launchDate;
    }

    public String getTitle() {
        return title;
    }

    public String getInstructors() {
        return instructors;
    }

    public String getSubject() {
        return subject;
    }

    public int getYear() {
        return year;
    }

    public int getHonorCode() {
        return honorCode;
    }

    public int getParticipants() {
        return participants;
    }

    public int getAudited() {
        return audited;
    }

    public int getCertified() {
        return certified;
    }

    public double getPercentAudited() {
        return percentAudited;
    }

    public double getPercentCertified() {
        return percentCertified;
    }

    public double getPercentCertified50() {
        return percentCertified50;
    }

    public double getPercentVideo() {
        return percentVideo;
    }

    public double getPercentForum() {
        return percentForum;
    }

    public double getGradeHigherZero() {
        return gradeHigherZero;
    }

    public double getTotalHours() {
        return totalHours;
    }

    public double getMedianHoursCertification() {
        return medianHoursCertification;
    }

    public double getMedianAge() {
        return medianAge;
    }

    public double getPercentMale() {
        return percentMale;
    }

    public double getPercentFemale() {
        return percentFemale;
    }

    public double getPercentDegree() {
        return percentDegree;
    }

    String institution;
    String number;
    Date launchDate;
    String title;
    String instructors;
    String subject;
    int year;
    int honorCode;
    int participants;
    int audited;
    int certified;
    double percentAudited;
    double percentCertified;
    double percentCertified50;
    double percentVideo;
    double percentForum;
    double gradeHigherZero;
    double totalHours;
    double medianHoursCertification;
    double medianAge;
    double percentMale;
    double percentFemale;
    double percentDegree;

    public Course(String institution, String number, Date launchDate,
                  String title, String instructors, String subject,
                  int year, int honorCode, int participants,
                  int audited, int certified, double percentAudited,
                  double percentCertified, double percentCertified50,
                  double percentVideo, double percentForum, double gradeHigherZero,
                  double totalHours, double medianHoursCertification,
                  double medianAge, double percentMale, double percentFemale,
                  double percentDegree) {
        this.institution = institution;
        this.number = number;
        this.launchDate = launchDate;
        if (title.startsWith("\"")) title = title.substring(1);
        if (title.endsWith("\"")) title = title.substring(0, title.length() - 1);
        this.title = title;
        if (instructors.startsWith("\"")) instructors = instructors.substring(1);
        if (instructors.endsWith("\"")) instructors = instructors.substring(0, instructors.length() - 1);
        this.instructors = instructors;
        if (subject.startsWith("\"")) subject = subject.substring(1);
        if (subject.endsWith("\"")) subject = subject.substring(0, subject.length() - 1);
        this.subject = subject;
        this.year = year;
        this.honorCode = honorCode;
        this.participants = participants;
        this.audited = audited;
        this.certified = certified;
        this.percentAudited = percentAudited;
        this.percentCertified = percentCertified;
        this.percentCertified50 = percentCertified50;
        this.percentVideo = percentVideo;
        this.percentForum = percentForum;
        this.gradeHigherZero = gradeHigherZero;
        this.totalHours = totalHours;
        this.medianHoursCertification = medianHoursCertification;
        this.medianAge = medianAge;
        this.percentMale = percentMale;
        this.percentFemale = percentFemale;
        this.percentDegree = percentDegree;
    }
}


 class Student {
    private String name;
    private int age;
    private String email;

    public Student(String name, int age, String email) {
        this.name = name;
        this.age = age;
        this.email = email;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getAge() {
        return age;
    }

    public void setAge(int age) {
        this.age = age;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }
}
