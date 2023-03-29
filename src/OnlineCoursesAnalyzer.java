import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.*;
import java.util.stream.Collectors;

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
                Course course = new Course(info[0], info[1], new Date(info[2]), info[3], info[4],
                    info[5],
                    Integer.parseInt(info[6]), Integer.parseInt(info[7]), Integer.parseInt(info[8]),
                    Integer.parseInt(info[9]), Integer.parseInt(info[10]),
                    Double.parseDouble(info[11]),
                    Double.parseDouble(info[12]), Double.parseDouble(info[13]),
                    Double.parseDouble(info[14]),
                    Double.parseDouble(info[15]), Double.parseDouble(info[16]),
                    Double.parseDouble(info[17]),
                    Double.parseDouble(info[18]), Double.parseDouble(info[19]),
                    Double.parseDouble(info[20]),
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
        Map<String, Integer> ptcpCountMap = new HashMap<>();
        for (Course course : courses) {
            String key = course.institution + "-" + course.subject;
            int value = ptcpCountMap.getOrDefault(key, 0);
            value += course.participants;
            ptcpCountMap.put(key, value);
        }

        List<Map.Entry<String, Integer>> list = new ArrayList<>(ptcpCountMap.entrySet());
        Collections.sort(list, (o1, o2) -> {
            if (o1.getValue().equals(o2.getValue())) {
                return o1.getKey().compareTo(o2.getKey());
            } else {
                return o2.getValue().compareTo(o1.getValue());
            }
        });
        Map<String, Integer> sortedPtcpCountMap = new LinkedHashMap<>();
        for (Map.Entry<String, Integer> entry : list) {
            sortedPtcpCountMap.put(entry.getKey(), entry.getValue());
        }
        return sortedPtcpCountMap;
    }


    //3
    public Map<String, List<List<String>>> getCourseListOfInstructor() {
        Map<String, List<List<String>>> instructorCourses = new HashMap<>();

        for (Course course : courses) {
            String instructorList = course.instructors;

            String[] instructorArray = instructorList.split(",");

            for (String instructor : instructorArray) {

                instructor = instructor.trim();

                if (!instructorCourses.containsKey(instructor)) {
                    instructorCourses.put(instructor, new ArrayList<>());
                    instructorCourses.get(instructor).add(new ArrayList<>());
                    instructorCourses.get(instructor).add(new ArrayList<>());
                }

                List<List<String>> instructorCourseList = instructorCourses.get(instructor);

                if (instructorList.equals(instructor)) {
                    List<String> independentCourses = instructorCourseList.get(0);
                    if (!independentCourses.contains(course.title)) {
                        independentCourses.add(course.title);
                        independentCourses.sort(null);
                    }
                } else {
                    List<String> collaborativeCourses = instructorCourseList.get(1);
                    if (!collaborativeCourses.contains(course.title)) {
                        collaborativeCourses.add(course.title);
                        collaborativeCourses.sort(null);
                    }
                }
            }
        }
        return instructorCourses;
    }

    //4
    public List<String> getCourses(int topK, String by) {
        List<Course> sortedCourses;
        if (by.equals("hours")) {
            sortedCourses = new ArrayList<>(courses);
            Collections.sort(sortedCourses, new Comparator<Course>() {
                @Override
                public int compare(Course c1, Course c2) {
                    int totalHoursCompare = Double.compare(c2.totalHours, c1.totalHours);
                    if (totalHoursCompare != 0) {
                        return totalHoursCompare;
                    }
                    return c1.title.compareTo(c2.title);
                }
            });
        } else if (by.equals("participants")) {
            sortedCourses = new ArrayList<>(courses);
            Collections.sort(sortedCourses, new Comparator<Course>() {
                @Override
                public int compare(Course c1, Course c2) {
                    int participantsCompare = Integer.compare(c2.participants, c1.participants);
                    if (participantsCompare != 0) {
                        return participantsCompare;
                    }
                    return c1.title.compareTo(c2.title);
                }
            });
        } else {
            throw new IllegalArgumentException(
                "Invalid sorting criteria. Please input 'hours' or 'participants'.");
        }

        List<String> topCourses = new ArrayList<>();
        Set<String> uniqueTitles = new HashSet<>();

        for (Course course : sortedCourses) {
            if (uniqueTitles.contains(course.title)) {
                continue;
            }

            topCourses.add(course.title);
            uniqueTitles.add(course.title);

            if (topCourses.size() == topK) {
                break;
            }
        }

        return topCourses;
    }

    //5
    public List<String> searchCourses(String courseSubject, double percentAudited, double totalCourseHours) {
        List<Course> matchingCourses = new ArrayList<>();
        for (Course course : courses) {
            if (course.subject.toLowerCase().contains(courseSubject.toLowerCase()) &&
                course.percentAudited >= percentAudited &&
                course.totalHours <= totalCourseHours) {

                matchingCourses.add(course);
            }
        }

        Collections.sort(matchingCourses, new Comparator<Course>() {
            public int compare(Course c1, Course c2) {
                return c1.title.compareTo(c2.title);
            }
        });

        List<String> matchingTitles = new ArrayList<>();
        Set<String> uniqueTitles = new HashSet<>();

        for (Course course : matchingCourses) {
            if (uniqueTitles.contains(course.title)) {
                continue;
            }

            matchingTitles.add(course.title);
            uniqueTitles.add(course.title);
        }

        return matchingTitles;
    }



    //6
    public List<String> recommendCourses(int age, int gender, int isBachelorOrHigher) {
        // Calculate the average median age, male percentage, and bachelor's degree or higher percentage of all courses
        double sumMedianAge = 0;
        double sumMale = 0;
        double sumBachelorOrHigher = 0;
        for (Course course : courses) {
            sumMedianAge += course.medianAge;
            sumMale += course.percentMale;
            sumBachelorOrHigher += course.percentDegree;
        }
        double avgMedianAge = sumMedianAge / courses.size();
        double avgMale = sumMale / courses.size();
        double avgBachelorOrHigher = sumBachelorOrHigher / courses.size();



// Calculate the similarity value of each course to the input user's features
        Map<Course, Double> similarityValues = new HashMap<>();
        for (Course course : courses) {
            double similarityValue = Math.pow(age - avgMedianAge, 2) + Math.pow(gender * 100 - avgMale, 2)
                + Math.pow(isBachelorOrHigher * 100 - avgBachelorOrHigher, 2);
            similarityValues.put(course, similarityValue);
        }

// Sort the courses by their similarity values in ascending order and get the top 10
        List<Course> recommendedCourses = similarityValues.entrySet().stream()
            .sorted(Map.Entry.comparingByValue())
            .map(Map.Entry::getKey)
            .limit(10)
            .collect(Collectors.toList());
// Sort the recommended courses by their launch date in descending order and get their titles

        Set<String> uniqueTitles = new HashSet<>();
        List<String> result = new ArrayList<>();
        recommendedCourses.sort(new Comparator<Course>() {
            @Override
            public int compare(Course o1, Course o2) {
                return o2.launchDate.compareTo(o1.launchDate);
            }
        });
        for (Course course : recommendedCourses) {
            if (!uniqueTitles.contains(course.title)) {
                uniqueTitles.add(course.title);
                result.add(course.title);
            }
        }
        return result;
    }
}


class Course {
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
            if (title.startsWith("\""))
                title = title.substring(1);
            if (title.endsWith("\""))
                title = title.substring(0, title.length() - 1);
            this.title = title;
            if (instructors.startsWith("\""))
                instructors = instructors.substring(1);
            if (instructors.endsWith("\""))
                instructors = instructors.substring(0, instructors.length() - 1);
            this.instructors = instructors;
            if (subject.startsWith("\""))
                subject = subject.substring(1);
            if (subject.endsWith("\""))
                subject = subject.substring(0, subject.length() - 1);
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













