package com.flooring.finance.seed;

import com.flooring.finance.common.JobStatus;
import com.flooring.finance.common.MalaysianState;
import com.flooring.finance.entity.DeliveryCost;
import com.flooring.finance.entity.Job;
import com.flooring.finance.entity.MaterialCost;
import com.flooring.finance.entity.OtherCost;
import com.flooring.finance.entity.User;
import com.flooring.finance.entity.WorkerCost;
import com.flooring.finance.entity.WorkerFoodCost;
import com.flooring.finance.repository.JobRepository;
import com.flooring.finance.repository.UserRepository;
import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDate;
import java.util.EnumMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import org.springframework.boot.CommandLineRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

/**
 * Development-only seed data: five hand-written flooring jobs (used
 * throughout the docs/screenshots) plus a larger batch of programmatically
 * generated ones spread across the last ~20 months, so the dashboard,
 * job list and reports all read like a real, ongoing business rather than a
 * handful of demo rows. Runs only under the "dev" profile and only if the
 * database is empty. Generation is seeded ({@link #RANDOM}) so the dataset
 * is identical on every fresh run.
 * <p>
 * <b>Seed login (development only - never used in production):</b>
 * username {@code owner}, password {@code ChangeMe123!}.
 */
@Component
@Profile("dev")
public class DataSeeder implements CommandLineRunner {

    private static final int GENERATED_JOB_COUNT = 65;
    private static final Random RANDOM = new Random(42);

    private final UserRepository userRepository;
    private final JobRepository jobRepository;
    private final PasswordEncoder passwordEncoder;

    public DataSeeder(UserRepository userRepository, JobRepository jobRepository, PasswordEncoder passwordEncoder) {
        this.userRepository = userRepository;
        this.jobRepository = jobRepository;
        this.passwordEncoder = passwordEncoder;
    }

    @Override
    @Transactional
    public void run(String... args) {
        if (userRepository.count() > 0) {
            return; // already seeded
        }

        userRepository.save(User.builder()
                .username("owner")
                .email("owner@flooringfinance.my")
                .fullName("Business Owner")
                .passwordHash(passwordEncoder.encode("ChangeMe123!"))
                .build());

        seedTamanMelawati();
        seedShahAlamOffice();
        seedKajangHouse();
        seedSerembanShop();
        seedJohorBahruProject();
        seedGeneratedJobs(GENERATED_JOB_COUNT);
    }

    // ------------------------------------------------------------------
    // Five hand-written jobs - referenced by the README and screenshots.
    // ------------------------------------------------------------------

    private void seedTamanMelawati() {
        Job job = Job.builder()
                .name("Taman Melawati House")
                .customerName("Ahmad")
                .location("Taman Melawati, Kuala Lumpur")
                .state(MalaysianState.KUALA_LUMPUR)
                .jobDate(LocalDate.of(2026, 5, 15))
                .status(JobStatus.COMPLETED)
                .notes("500 sq ft SPC vinyl plank, living and dining area.")
                .collectionAmount(new BigDecimal("15000.00"))
                .build();

        addMaterial(job, "Flooring", "6000.00");
        addMaterial(job, "Glue", "300.00");
        addMaterial(job, "Underlay", "200.00");

        addDelivery(job, "Lalamove", "250.00", LocalDate.of(2026, 5, 14));
        addDelivery(job, "Other Delivery", "200.00", LocalDate.of(2026, 5, 16));

        addOther(job, "Petrol", "150.00", "Petrol", LocalDate.of(2026, 5, 15));
        addOther(job, "Guni", "30.00", "Supplies", LocalDate.of(2026, 5, 15));
        addOther(job, "Parking", "20.00", "Parking", LocalDate.of(2026, 5, 15));
        addOther(job, "Miscellaneous", "50.00", "Other", LocalDate.of(2026, 5, 15));

        addWorker(job, "Ali", "500.00");
        addWorker(job, "Abu", "500.00");
        addWorker(job, "Rahman", "400.00");

        addFood(job, LocalDate.of(2026, 5, 15), "80.00");
        addFood(job, LocalDate.of(2026, 5, 16), "80.00");
        addFood(job, LocalDate.of(2026, 5, 17), "80.00");

        jobRepository.save(job);
    }

    private void seedShahAlamOffice() {
        Job job = Job.builder()
                .name("Shah Alam Office")
                .customerName("Tan Wei Ming")
                .location("Seksyen 15, Shah Alam")
                .state(MalaysianState.SELANGOR)
                .jobDate(LocalDate.of(2026, 6, 10))
                .status(JobStatus.COMPLETED)
                .notes("Office renovation - laminate flooring throughout.")
                .collectionAmount(new BigDecimal("20000.00"))
                .build();

        addMaterial(job, "Laminate Flooring", "8500.00");
        addMaterial(job, "Adhesive & Underlay", "500.00");

        addDelivery(job, "Lalamove", "500.00", LocalDate.of(2026, 6, 9));
        addDelivery(job, "Lorry Rental", "300.00", LocalDate.of(2026, 6, 9));

        addOther(job, "Petrol", "200.00", "Petrol", LocalDate.of(2026, 6, 10));
        addOther(job, "Parking", "100.00", "Parking", LocalDate.of(2026, 6, 10));
        addOther(job, "Tools", "200.00", "Tools", LocalDate.of(2026, 6, 11));

        addWorker(job, "Ali", "800.00");
        addWorker(job, "Abu", "800.00");
        addWorker(job, "Rahman", "800.00");
        addWorker(job, "Kumar", "800.00");

        addFood(job, LocalDate.of(2026, 6, 10), "120.00");
        addFood(job, LocalDate.of(2026, 6, 11), "120.00");
        addFood(job, LocalDate.of(2026, 6, 12), "120.00");
        addFood(job, LocalDate.of(2026, 6, 13), "140.00");

        jobRepository.save(job);
    }

    private void seedKajangHouse() {
        Job job = Job.builder()
                .name("Kajang House")
                .customerName("Siti Rahman")
                .location("Bandar Kajang, Selangor")
                .state(MalaysianState.SELANGOR)
                .jobDate(LocalDate.of(2026, 7, 5))
                .status(JobStatus.COMPLETED)
                .notes("Vinyl flooring for 3-bedroom house.")
                .collectionAmount(new BigDecimal("13500.00"))
                .build();

        addMaterial(job, "SPC Vinyl Plank", "5000.00");
        addMaterial(job, "Underlay", "500.00");

        addDelivery(job, "Lalamove", "300.00", LocalDate.of(2026, 7, 4));

        addOther(job, "Petrol", "100.00", "Petrol", LocalDate.of(2026, 7, 5));
        addOther(job, "Guni", "20.00", "Supplies", LocalDate.of(2026, 7, 5));
        addOther(job, "Miscellaneous", "80.00", "Other", LocalDate.of(2026, 7, 6));

        addWorker(job, "Ali", "700.00");
        addWorker(job, "Abu", "700.00");
        addWorker(job, "Rahman", "600.00");

        addFood(job, LocalDate.of(2026, 7, 5), "180.00");
        addFood(job, LocalDate.of(2026, 7, 6), "180.00");

        jobRepository.save(job);
    }

    private void seedSerembanShop() {
        Job job = Job.builder()
                .name("Seremban Shop")
                .customerName("Lim Kok Wei")
                .location("Seremban 2, Negeri Sembilan")
                .state(MalaysianState.NEGERI_SEMBILAN)
                .jobDate(LocalDate.of(2026, 8, 1))
                .status(JobStatus.IN_PROGRESS)
                .notes("Retail shop lot - homogeneous tile flooring. Deposit collected, work ongoing.")
                .collectionAmount(new BigDecimal("18000.00"))
                .build();

        addMaterial(job, "Homogeneous Tiles", "6500.00");
        addMaterial(job, "Tile Adhesive & Grout", "500.00");

        addDelivery(job, "Lalamove", "400.00", LocalDate.of(2026, 7, 31));

        addOther(job, "Petrol", "150.00", "Petrol", LocalDate.of(2026, 8, 1));
        addOther(job, "Parking", "50.00", "Parking", LocalDate.of(2026, 8, 1));
        addOther(job, "Toll", "100.00", "Other", LocalDate.of(2026, 8, 1));

        addWorker(job, "Ali", "600.00");
        addWorker(job, "Rahman", "600.00");
        addWorker(job, "Kumar", "600.00");

        addFood(job, LocalDate.of(2026, 8, 1), "90.00");
        addFood(job, LocalDate.of(2026, 8, 2), "90.00");

        jobRepository.save(job);
    }

    private void seedJohorBahruProject() {
        Job job = Job.builder()
                .name("Johor Bahru Project")
                .customerName("Rajesh Kumar")
                .location("Taman Molek, Johor Bahru")
                .state(MalaysianState.JOHOR)
                .jobDate(LocalDate.of(2026, 8, 8))
                .status(JobStatus.IN_PROGRESS)
                .notes("Commercial unit - parquet flooring for reception and offices.")
                .collectionAmount(new BigDecimal("22000.00"))
                .build();

        addMaterial(job, "Solid Parquet", "9500.00");
        addMaterial(job, "Finishing & Adhesive", "500.00");

        addDelivery(job, "Lalamove", "350.00", LocalDate.of(2026, 8, 7));
        addDelivery(job, "Lorry Rental", "250.00", LocalDate.of(2026, 8, 7));

        addOther(job, "Petrol", "220.00", "Petrol", LocalDate.of(2026, 8, 8));
        addOther(job, "Toll", "80.00", "Other", LocalDate.of(2026, 8, 8));
        addOther(job, "Miscellaneous", "100.00", "Other", LocalDate.of(2026, 8, 9));

        addWorker(job, "Ali", "900.00");
        addWorker(job, "Abu", "900.00");
        addWorker(job, "Rahman", "700.00");

        addFood(job, LocalDate.of(2026, 8, 8), "150.00");
        addFood(job, LocalDate.of(2026, 8, 9), "150.00");

        jobRepository.save(job);
    }

    // ------------------------------------------------------------------
    // Bulk-generated jobs - same shape of data, spread across ~20 months.
    // ------------------------------------------------------------------

    private static final String[] CUSTOMER_NAMES = {
            "Nurul Ain", "Wong Mei Ling", "Muthu Samy", "Chong Ah Seng", "Fatimah Zahra",
            "Hafiz Rahman", "Cheah Boon Huat", "Aisyah Ismail", "Kumar Selvam", "Ng Wei Jian",
            "Zainab Hussain", "Teoh Beng Hock", "Priya Devi", "Amirul Haziq", "Lee Chong Wei",
            "Balasubramaniam", "Yeoh Siew Fong", "Ismail Kassim", "Chan Kar Weng", "Farah Diyana",
            "Ravindran Nair", "Ooi Poh Ling", "Azman Yusof", "Tan Sri Lim", "Suresh Kumar",
            "Halimah Yacob", "Goh Chin Huat", "Nabila Sharif", "Vijayakumar", "Foo Yee Ching",
            "Rosnah Ibrahim", "Lau Tuck Meng", "Shanmugam Pillai", "Aina Syafiqah", "Chua Beng Guan",
            "Maimunah Salleh", "Devan Raj", "Loh Kok Seng", "Nurhaliza Othman", "Chin Wai Keong",
            "Anitha Rajoo", "Hasnah Daud", "Koh Boon Lay", "Faridah Merican", "Sivakumar",
        };

    private static final Map<MalaysianState, String[]> LOCATIONS_BY_STATE = buildLocations();

    private static Map<MalaysianState, String[]> buildLocations() {
        Map<MalaysianState, String[]> m = new EnumMap<>(MalaysianState.class);
        m.put(MalaysianState.JOHOR, new String[]{"Skudai", "Batu Pahat", "Muar", "Kulai", "Taman Molek, Johor Bahru"});
        m.put(MalaysianState.KEDAH, new String[]{"Alor Setar", "Sungai Petani", "Kulim"});
        m.put(MalaysianState.KELANTAN, new String[]{"Kota Bharu", "Pasir Mas"});
        m.put(MalaysianState.MELAKA, new String[]{"Melaka City", "Ayer Keroh", "Alor Gajah"});
        m.put(MalaysianState.NEGERI_SEMBILAN, new String[]{"Seremban 2, Negeri Sembilan", "Nilai", "Port Dickson"});
        m.put(MalaysianState.PAHANG, new String[]{"Kuantan", "Temerloh", "Bentong"});
        m.put(MalaysianState.PENANG, new String[]{"George Town", "Bayan Lepas", "Butterworth", "Bukit Mertajam"});
        m.put(MalaysianState.PERAK, new String[]{"Ipoh", "Taiping", "Sitiawan"});
        m.put(MalaysianState.PERLIS, new String[]{"Kangar"});
        m.put(MalaysianState.SABAH, new String[]{"Kota Kinabalu", "Sandakan"});
        m.put(MalaysianState.SARAWAK, new String[]{"Kuching", "Miri"});
        m.put(MalaysianState.SELANGOR, new String[]{"Shah Alam", "Petaling Jaya", "Subang Jaya", "Klang", "Kajang", "Cheras", "Puchong", "Rawang"});
        m.put(MalaysianState.TERENGGANU, new String[]{"Kuala Terengganu", "Dungun"});
        m.put(MalaysianState.KUALA_LUMPUR, new String[]{"Bukit Bintang", "Ampang", "Setapak", "Taman Melawati, Kuala Lumpur", "Bangsar", "Cheras KL"});
        m.put(MalaysianState.LABUAN, new String[]{"Labuan"});
        m.put(MalaysianState.PUTRAJAYA, new String[]{"Putrajaya"});
        return m;
    }

    // Peninsular Malaysia only, matching this business's documented coverage
    // (Settings -> Business Defaults) - Sabah, Sarawak and Labuan are real
    // MalaysianState values (a job could still be entered there by hand) but
    // the generator shouldn't invent jobs outside where the business operates.
    private static final MalaysianState[] STATES = {
            MalaysianState.JOHOR, MalaysianState.KEDAH, MalaysianState.KELANTAN, MalaysianState.MELAKA,
            MalaysianState.NEGERI_SEMBILAN, MalaysianState.PAHANG, MalaysianState.PENANG, MalaysianState.PERAK,
            MalaysianState.PERLIS, MalaysianState.SELANGOR, MalaysianState.TERENGGANU, MalaysianState.KUALA_LUMPUR,
            MalaysianState.PUTRAJAYA,
    };

    private static final String[] JOB_KINDS = {"House", "Office", "Shop", "Condo", "Apartment", "Renovation", "Terrace House", "Bungalow"};

    /** Flooring type -> [material line description, notes fragment]. */
    private static final String[][] FLOORING_TYPES = {
            {"SPC Vinyl Plank", "SPC vinyl plank flooring"},
            {"Laminate Flooring", "laminate flooring"},
            {"Homogeneous Tiles", "homogeneous tile flooring"},
            {"Solid Parquet", "solid parquet flooring"},
            {"Engineered Wood Flooring", "engineered wood flooring"},
            {"Vinyl Sheet", "vinyl sheet flooring"},
            {"Ceramic Tiles", "ceramic tile flooring"},
            {"Carpet Tiles", "carpet tile flooring"},
    };

    private static final String[] AREAS = {
            "living and dining area", "whole unit", "bedrooms and hallway",
            "reception and offices", "retail floor", "kitchen and utility area", "common areas",
    };

    private static final String[] WORKER_NAMES = {
            "Ali", "Abu", "Rahman", "Kumar", "Hafiz", "Zul", "Azman", "Faizal", "Chong", "Wei", "Suresh", "Hakim", "Farid", "Din",
    };

    private static final String[][] OTHER_COST_TEMPLATES = {
            {"Petrol", "Petrol"}, {"Parking", "Parking"}, {"Toll", "Other"}, {"Tools", "Tools"},
            {"Guni / Supplies", "Supplies"}, {"Utilities", "Utilities"}, {"Miscellaneous", "Other"},
    };

    private void seedGeneratedJobs(int count) {
        LocalDate today = LocalDate.now();

        for (int i = 0; i < count; i++) {
            MalaysianState state = STATES[RANDOM.nextInt(STATES.length)];
            String[] locations = LOCATIONS_BY_STATE.get(state);
            String location = locations[RANDOM.nextInt(locations.length)];
            String kind = JOB_KINDS[RANDOM.nextInt(JOB_KINDS.length)];
            String customer = CUSTOMER_NAMES[RANDOM.nextInt(CUSTOMER_NAMES.length)];
            String[] flooring = FLOORING_TYPES[RANDOM.nextInt(FLOORING_TYPES.length)];
            String area = AREAS[RANDOM.nextInt(AREAS.length)];

            int daysAgo = RANDOM.nextInt(610); // spread across ~20 months
            LocalDate jobDate = today.minusDays(daysAgo);

            JobStatus status;
            if (daysAgo <= 21) {
                double r = RANDOM.nextDouble();
                status = r < 0.55 ? JobStatus.IN_PROGRESS : (r < 0.90 ? JobStatus.COMPLETED : JobStatus.CANCELLED);
            } else {
                status = RANDOM.nextDouble() < 0.92 ? JobStatus.COMPLETED : JobStatus.CANCELLED;
            }

            // Job "size" tier drives every other amount so costs stay proportionate.
            BigDecimal collection = randomRoundAmount(6000, 45000);
            int sqft = 300 + RANDOM.nextInt(1400);

            Job job = Job.builder()
                    .name(location + " " + kind)
                    .customerName(customer)
                    .location(location)
                    .state(state)
                    .jobDate(jobDate)
                    .status(status)
                    .notes(sqft + " sq ft " + flooring[1] + ", " + area + ".")
                    .collectionAmount(collection)
                    .build();

            // Materials: 1-3 line items summing to ~32-45% of collection.
            BigDecimal materialsTotal = pct(collection, 0.32, 0.45);
            List<BigDecimal> materialSplits = splitAmount(materialsTotal, 1 + RANDOM.nextInt(3));
            addMaterial(job, flooring[0], materialSplits.get(0).toPlainString());
            if (materialSplits.size() > 1) {
                addMaterial(job, "Adhesive & Underlay", materialSplits.get(1).toPlainString());
            }
            if (materialSplits.size() > 2) {
                addMaterial(job, "Finishing & Trims", materialSplits.get(2).toPlainString());
            }

            // Delivery: 1-2 line items, a day or two before the job date.
            BigDecimal deliveryTotal = pct(collection, 0.015, 0.035);
            List<BigDecimal> deliverySplits = splitAmount(deliveryTotal, 1 + RANDOM.nextInt(2));
            addDelivery(job, "Lalamove", deliverySplits.get(0).toPlainString(), jobDate.minusDays(1));
            if (deliverySplits.size() > 1) {
                addDelivery(job, "Lorry Rental", deliverySplits.get(1).toPlainString(), jobDate.minusDays(1));
            }

            // Other costs: 2-4 small line items on/around the job date.
            BigDecimal otherTotal = pct(collection, 0.01, 0.03);
            int otherCount = 2 + RANDOM.nextInt(3);
            List<BigDecimal> otherSplits = splitAmount(otherTotal, otherCount);
            for (int j = 0; j < otherCount; j++) {
                String[] tmpl = OTHER_COST_TEMPLATES[RANDOM.nextInt(OTHER_COST_TEMPLATES.length)];
                addOther(job, tmpl[0], otherSplits.get(j).toPlainString(), tmpl[1], jobDate.plusDays(RANDOM.nextInt(2)));
            }

            // Worker salary: 2-4 distinct workers.
            BigDecimal salaryTotal = pct(collection, 0.10, 0.18);
            int workerCount = 2 + RANDOM.nextInt(3);
            List<BigDecimal> salarySplits = splitAmount(salaryTotal, workerCount);
            List<String> pickedWorkers = distinctWorkers(workerCount);
            for (int j = 0; j < workerCount; j++) {
                addWorker(job, pickedWorkers.get(j), salarySplits.get(j).toPlainString());
            }

            // Worker food: 1-4 daily entries around the job date.
            BigDecimal foodTotal = pct(collection, 0.008, 0.018);
            int foodDays = 1 + RANDOM.nextInt(4);
            List<BigDecimal> foodSplits = splitAmount(foodTotal, foodDays);
            for (int j = 0; j < foodDays; j++) {
                addFood(job, jobDate.plusDays(j), foodSplits.get(j).toPlainString());
            }

            jobRepository.save(job);
        }
    }

    private List<String> distinctWorkers(int n) {
        List<String> pool = new java.util.ArrayList<>(List.of(WORKER_NAMES));
        java.util.Collections.shuffle(pool, RANDOM);
        return pool.subList(0, n);
    }

    /** A random amount, rounded to the nearest 100, between min and max (inclusive-ish). */
    private BigDecimal randomRoundAmount(int min, int max) {
        int raw = min + RANDOM.nextInt(max - min);
        int rounded = Math.round(raw / 100f) * 100;
        return new BigDecimal(rounded).setScale(2, RoundingMode.HALF_UP);
    }

    /** {@code base * a random percentage in [minPct, maxPct]}, rounded to 2dp. */
    private BigDecimal pct(BigDecimal base, double minPct, double maxPct) {
        double p = minPct + RANDOM.nextDouble() * (maxPct - minPct);
        return base.multiply(BigDecimal.valueOf(p)).setScale(2, RoundingMode.HALF_UP);
    }

    /** Splits an amount into {@code parts} positive shares (randomized, but summing back to the original). */
    private List<BigDecimal> splitAmount(BigDecimal total, int parts) {
        if (parts <= 1) {
            return List.of(total.setScale(2, RoundingMode.HALF_UP));
        }
        double[] weights = new double[parts];
        double weightSum = 0;
        for (int i = 0; i < parts; i++) {
            weights[i] = 0.4 + RANDOM.nextDouble();
            weightSum += weights[i];
        }
        List<BigDecimal> result = new java.util.ArrayList<>();
        BigDecimal running = BigDecimal.ZERO;
        for (int i = 0; i < parts - 1; i++) {
            BigDecimal share = total.multiply(BigDecimal.valueOf(weights[i] / weightSum)).setScale(2, RoundingMode.HALF_UP);
            result.add(share);
            running = running.add(share);
        }
        result.add(total.subtract(running).setScale(2, RoundingMode.HALF_UP)); // remainder, keeps the total exact
        return result;
    }

    private void addMaterial(Job job, String description, String amount) {
        job.getMaterialCosts().add(MaterialCost.builder().job(job).description(description).amount(new BigDecimal(amount)).build());
    }

    private void addDelivery(Job job, String description, String amount, LocalDate date) {
        job.getDeliveryCosts().add(DeliveryCost.builder().job(job).description(description).amount(new BigDecimal(amount)).date(date).build());
    }

    private void addOther(Job job, String description, String amount, String category, LocalDate date) {
        job.getOtherCosts().add(OtherCost.builder().job(job).description(description).amount(new BigDecimal(amount)).category(category).date(date).build());
    }

    private void addWorker(Job job, String workerName, String amount) {
        job.getWorkerCosts().add(WorkerCost.builder().job(job).workerName(workerName).amount(new BigDecimal(amount)).build());
    }

    private void addFood(Job job, LocalDate date, String amount) {
        job.getWorkerFoodCosts().add(WorkerFoodCost.builder().job(job).date(date).description("Worker meals").amount(new BigDecimal(amount)).build());
    }
}
