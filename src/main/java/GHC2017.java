package main.java;

import com.sun.jdi.IntegerValue;

import java.io.*;
import java.util.*;

public class GHC2017 {

    public Map<String, Object> data;

    public List<List<List<Integer>>> Population = new ArrayList<>();

    public GHC2017() {
        data = new HashMap<String, Object>();
    }

    public int popSize = 100;

    public int mutationChance = 10;

    /**
     * 
     */
    public List<List<Integer>> hillClimb(List<List<Integer>> solution) {
        int[] videoSizes = (int[]) data.get("video_size_desc");
        boolean betterSolutionFound = false;

        List<List<Integer>> currentBestSolution = new ArrayList<List<Integer>>();
        for(int c = 0; c < solution.size(); c++) {
            currentBestSolution.add(new ArrayList<Integer>());
            for (int f = 0; f < videoSizes.length; f++) {
                currentBestSolution.get(c).add(solution.get(c).get(f));
            }
        }

        //System.out.println(videoSizes.length);

        for(int c = 0; c < solution.size(); c++) {
            for (int f = 0; f < videoSizes.length; f = f + (int )Math.round((videoSizes.length/100)-0.5)) {

                List<List<Integer>> tempSolution = new ArrayList<List<Integer>>();
                tempSolution.removeAll(tempSolution);
                for(int c1 = 0; c1 < solution.size(); c1++) {
                    tempSolution.add(new ArrayList<Integer>());
                    for (int f1 = 0; f1 < videoSizes.length; f1++) {
                        tempSolution.get(c1).add(solution.get(c1).get(f1));
                    }
                }

                if (Population.size() != popSize && fitness(tempSolution) != -1) Population.add(tempSolution);

                //System.out.println("Current Challenger:  " + fitness(tempSolution));
                //System.out.println("Current Best:  " + fitness(currentBestSolution));

                if (tempSolution.get(c).get(f) == 0) {
                    tempSolution.get(c).set(f, 1);
                } else {
                    tempSolution.get(c).set(f, 0);
                }

                if (fitness(tempSolution) > fitness(currentBestSolution) && fitness(tempSolution) != -1) {
                    currentBestSolution = new ArrayList<List<Integer>>();
                    for(int c1 = 0; c1 < tempSolution.size(); c1++) {
                        currentBestSolution.add(new ArrayList<Integer>());
                        for (int f1 = 0; f1 < videoSizes.length; f1++) {
                            currentBestSolution.get(c1).add(tempSolution.get(c1).get(f1));
                        }
                    }

                    betterSolutionFound = true;
                }

            }

            if ((int )Math.round((videoSizes.length/100)-0.5) > 10 && betterSolutionFound) {
                System.out.println("Got here: Recursion");
                Population.set(0,currentBestSolution);
                return hillClimb(currentBestSolution);
            }
        }
        if (betterSolutionFound) {
            Population.set(0,currentBestSolution);
            return hillClimb(currentBestSolution);
        } else {
            Collections.reverse(Population);
            //System.out.println("Pop size: " + Population.size());
            return currentBestSolution;
        }
    }

    public int fitness(List<List<Integer>> solution) {

        double score = 0.0;
        int[] videoSizes = (int[]) data.get("video_size_desc");

        int validLength = videoSizes.length * (Integer) data.get("number_of_caches");

        //System.out.println("Size of Solution :" + solution.size() * solution.get(0).size() + " VS Valid Length :" + validLength);
        if (solution.size() * solution.get(0).size() < validLength) return -1;

        for(int c = 0; c < solution.size(); c++) {
            int memoryUsage = 0;
            for (int f = 0; f < videoSizes.length; f++) {
                if (solution.get(c).get(f) == 1) {
                    memoryUsage += videoSizes[f];
                }
            }
            if (memoryUsage > (Integer) data.get("cache_size")) {
                return -1;
            }
        }

        Map<String, String> video_ed_request = (Map<String, String>) data.get("video_ed_request");
        List<Integer> ep_to_dc_latency = (List<Integer>) data.get("ep_to_dc_latency");

        int totalRequests = 0;

        for (Map.Entry<String,String> request : video_ed_request.entrySet()) {
            String key = request.getKey();
            String[] splitKey = key.split(",");

            int fileID = Integer.parseInt(splitKey[0]);
            int endpointID = Integer.parseInt(splitKey[1]);
            int numberOfRequests = Integer.parseInt(request.getValue());
            totalRequests += numberOfRequests;

            int dcLatency =((List<Integer>) data.get("ep_to_dc_latency")).get(endpointID);

            List<Integer> cacheLatencyList = ((List<List<Integer>>) data.get("ep_to_cache_latency")).get(endpointID);
            int cacheLatency = dcLatency;


            for (int cache : cacheLatencyList) {
                if (cacheLatencyList.indexOf(cache) < solution.size()) {
                    if (cache < cacheLatency && solution.get(cacheLatencyList.indexOf(cache)).get(fileID) == 1) {
                        cacheLatency = cache;
                    }
                }
            }
            if (cacheLatency != dcLatency) {
                score += numberOfRequests * (dcLatency - cacheLatency);
            }
        }
        return ((int) Math.round((score/totalRequests)*1000));
    }

    public List<List<Integer>> generateSolution(int numberOfVideos, int numberOfCaches) {
        List<List<Integer>> solution = new ArrayList<List<Integer>>();

            for (int c = 0; c < numberOfCaches; c++) {
                solution.add(new ArrayList<Integer>());
                    for (int f = 0; f < numberOfVideos; f++) {
                            solution.get(c).add(0);
                    }
            }
        Population.add(solution);
        solution = hillClimb(solution);
        return solution;
    }
    public List<List<List<Integer>>> splice (List<List<Integer>> mother, List<List<Integer>> father) {

        List<List<List<Integer>>> children = new ArrayList<>();

        //System.out.println("Checking Length of sublist: " + mother.subList(0, (mother.size()-1)/2).size());
        //System.out.println("Parent Fitness: Father = " + fitness(father) + " Mother = " + fitness(mother));

        List<List<Integer>> motherSnippet1 = mother.subList(0, (mother.size()-1)/2);
        List<List<Integer>> motherSnippet2 = mother.subList((mother.size()-1)/2, mother.size());

        List<List<Integer>> fatherSnippet1 = father.subList(0, (father.size()-1)/2);
        List<List<Integer>> fatherSnippet2 = father.subList((father.size()-1)/2, father.size());

        List<List<Integer>> child_1 = new ArrayList<>();
        List<List<Integer>> child_1_temp = new ArrayList<>();
        child_1_temp.addAll(motherSnippet1);
        child_1_temp.addAll(fatherSnippet2);
        for(int c = 0; c < child_1_temp.size(); c++) {
            child_1.add(new ArrayList<Integer>());
            for (int f = 0; f < ((int[]) data.get("video_size_desc")).length; f++) {
                child_1.get(c).add(child_1_temp.get(c).get(f));
            }
        }
        children.add(child_1);

        List<List<Integer>> child_2 = new ArrayList<>();
        List<List<Integer>> child_2_temp = new ArrayList<>();
        child_2_temp.addAll(fatherSnippet1);
        child_2_temp.addAll(motherSnippet2);
        for(int c = 0; c < child_2_temp.size(); c++) {
            child_2.add(new ArrayList<Integer>());
            for (int f = 0; f < ((int[]) data.get("video_size_desc")).length; f++) {
                child_2.get(c).add(child_2_temp.get(c).get(f));
            }
        }
        children.add(child_2);

        long mutation = (Math.round(Math.random()*(((Integer) data.get("number_of_caches")) * ((Integer) data.get("number_of_videos")))/mutationChance));
        if ((int) mutation != 1) {
            mutation = (Math.round(Math.random()*(((Integer) data.get("number_of_caches")) * ((Integer) data.get("number_of_videos")))/mutationChance));
            if ((int) mutation == 1) {
                return mutate(children, false, true);
            }
            return children;
        } else {
            mutation = (Math.round(Math.random()*(((Integer) data.get("number_of_caches")) * ((Integer) data.get("number_of_videos")))/mutationChance));
            if ((int) mutation == 1) {
                return mutate(children, true, true);
            }
            return mutate(children, true, false);
        }
    }

    public List<List<List<Integer>>> mutate (List<List<List<Integer>>> children, boolean first, boolean second) {
        int cache = (int) Math.round(Math.random() * ((Integer) data.get("number_of_caches")));
        int file = (int) Math.round(Math.random() * ((Integer) data.get("number_of_videos")));

        if (cache == (Integer) data.get("number_of_caches")) cache--;
        if (file == (Integer) data.get("number_of_videos")) file--;

        //System.out.println("Cache Number " + cache);
        //System.out.println("File Number " + file);

        if (first) {
            if (children.get(0).get(cache).get(file) == 1) {
                children.get(0).get(cache).set(file, 0);
            } else {
                children.get(0).get(cache).set(file, 1);
            }
        }

        cache = (int) Math.round(Math.random() * (((Integer) data.get("number_of_caches")) - 1));
        file = (int) Math.round(Math.random() * (((Integer) data.get("number_of_videos")) - 1));

        if (cache == (Integer) data.get("number_of_caches")) cache--;
        if (file == (Integer) data.get("number_of_videos")) file--;

        //System.out.println("Cache Number " + cache);
        //System.out.println("File Number " + file);

        if (second) {
            if (children.get(1).get(cache).get(file) == 1) {
                children.get(1).get(cache).set(file, 0);
            } else {
                children.get(1).get(cache).set(file, 1);
            }
        }
        return children;
    }

    public List<List<Integer>> evolution(int generation) {
       boolean motherSelected = false;
       boolean fatherSelected = false;

        List<List<Integer>> mother = new ArrayList<>();
        List<List<Integer>> father = new ArrayList<>();

        List<List<List<Integer>>> nextGen = new ArrayList<>();

        if (generation <= 2000) {

            for (int i=0; i < Population.size() - 1;i=i+2){
                if (fitness(Population.get(i)) != -1 && !motherSelected) {
                    mother = Population.get(i);
                    motherSelected = true;
                }
                if (fitness(Population.get(i)) != -1 && !fatherSelected) {
                    father = Population.get(i + 1);
                    fatherSelected = true;
                }

                if (motherSelected && fatherSelected) {
                    List<List<List<Integer>>> children = splice(mother, father);
                    if (fitness(children.get(0)) != -1) {
                        nextGen.add(children.get(0));
                    }
                    if (fitness(children.get(1)) != -1) {
                        nextGen.add(children.get(1));
                    }
                }
                motherSelected = false;
                fatherSelected = false;
            }
            Population.addAll(nextGen);
            Cull();

            //System.out.println("Generation Number: " + generation);
            evolution(generation + 1);
        }
        List<List<Integer>> darwin = new ArrayList<>(Population.get(0));
        for (int i=1; i < Population.size(); i++) {
            if (fitness(Population.get(i)) > fitness(darwin)) {
                darwin = Population.get(i);
            }
        }
        return darwin;
    }

    public void Cull() {
        ArrayList<FitnessIndexPair> fitnessMap = new ArrayList();

        for (int i=0; i < Population.size()-1; i++) {
            FitnessIndexPair entry = new FitnessIndexPair(i, fitness(Population.get(i)));
            fitnessMap.add(entry);
        }

        Collections.sort(fitnessMap, Comparator.comparing(FitnessIndexPair::getFitness));

        for(int i=0; i<=(Population.size()-1)-(popSize);i++) {
            Population.set(fitnessMap.get(i).getIndex(), new ArrayList<>());
        }
        for(int i=0; i<Population.size();i++) {
            if (Population.get(i).size() == 0) {
                Population.remove(i);
                i--;
            }
        }
    }

    public void readGoogle(String filename) throws IOException {
        
       // Map<String, Object> data = new HashMap<String, Object>();
    
        BufferedReader fin = new BufferedReader(new FileReader(filename));
    
        String system_desc = fin.readLine();
        String[] system_desc_arr = system_desc.split(" ");
        int number_of_videos = Integer.parseInt(system_desc_arr[0]);
        int number_of_endpoints = Integer.parseInt(system_desc_arr[1]);
        int number_of_requests = Integer.parseInt(system_desc_arr[2]);
        int number_of_caches = Integer.parseInt(system_desc_arr[3]);
        int cache_size = Integer.parseInt(system_desc_arr[4]);
    
        Map<String, String> video_ed_request = new HashMap<String, String>();
        String video_size_desc_str = fin.readLine();
        String[] video_size_desc_arr = video_size_desc_str.split(" ");
        int[] video_size_desc = new int[video_size_desc_arr.length];
        for (int i = 0; i < video_size_desc_arr.length; i++) {
            video_size_desc[i] = Integer.parseInt(video_size_desc_arr[i]);
        }
    
        List<List<Integer>> ed_cache_list = new ArrayList<List<Integer>>();
        List<Integer> ep_to_dc_latency = new ArrayList<Integer>();
        List<List<Integer>> ep_to_cache_latency = new ArrayList<List<Integer>>();
        for (int i = 0; i < number_of_endpoints; i++) {
            ep_to_dc_latency.add(0);
            ep_to_cache_latency.add(new ArrayList<Integer>());
    
            String[] endpoint_desc_arr = fin.readLine().split(" ");
            int dc_latency = Integer.parseInt(endpoint_desc_arr[0]);
            int number_of_cache_i = Integer.parseInt(endpoint_desc_arr[1]);
            ep_to_dc_latency.set(i, dc_latency);
    
            for (int j = 0; j < number_of_caches; j++) {
                ep_to_cache_latency.get(i).add(ep_to_dc_latency.get(i) + 1);
            }
    
            List<Integer> cache_list = new ArrayList<Integer>();
            for (int j = 0; j < number_of_cache_i; j++) {
                String[] cache_desc_arr = fin.readLine().split(" ");
                int cache_id = Integer.parseInt(cache_desc_arr[0]);
                int latency = Integer.parseInt(cache_desc_arr[1]);
                cache_list.add(cache_id);
                ep_to_cache_latency.get(i).set(cache_id, latency);
            }
            ed_cache_list.add(cache_list);
        }
    
        for (int i = 0; i < number_of_requests; i++) {
            String[] request_desc_arr = fin.readLine().split(" ");
            String video_id = request_desc_arr[0];
            String ed_id = request_desc_arr[1];
            String requests = request_desc_arr[2];
            video_ed_request.put(video_id + "," + ed_id, requests);
        }
    
        data.put("number_of_videos", number_of_videos);
        data.put("number_of_endpoints", number_of_endpoints);
        data.put("number_of_requests", number_of_requests);
        data.put("number_of_caches", number_of_caches);
        data.put("cache_size", cache_size);
        data.put("video_size_desc", video_size_desc);
        data.put("ep_to_dc_latency", ep_to_dc_latency);
        data.put("ep_to_cache_latency", ep_to_cache_latency);
        data.put("ed_cache_list", ed_cache_list);
        data.put("video_ed_request", video_ed_request);
    
        fin.close();
    
    }
    
    public String toString() {
        String result = "";

        //for each endpoint: 
        for(int i = 0; i < (Integer) data.get("number_of_endpoints"); i++) {
            result += "endpoint number " + i + "\n";
            //latency to DC
            int latency_dc = ((List<Integer>) data.get("ep_to_dc_latency")).get(i);
            result += "latency to dc = " + latency_dc + "\n";
            //for each cache
            for(int j = 0; j < ((List<List<Integer>>) data.get("ep_to_cache_latency")).get(i).size(); j++) {
                int latency_c = ((List<List<Integer>>) data.get("ep_to_cache_latency")).get(i).get(j); 
                result += "latency to cache number " + j + " = " + latency_c + "\n";
            }
        }

        //result += 

        return result;
    }

    public static void main(String[] args) throws IOException {  
        GHC2017 gh = new GHC2017();
        //gh.readGoogle("input/example.in");
        gh.readGoogle("input/me_at_the_zoo.in");
        System.out.println(gh.toString());
        List<List<Integer>> mySolution;
        mySolution = gh.generateSolution((Integer) gh.data.get("number_of_videos"), (Integer) gh.data.get("number_of_caches"));
        int hillScore = gh.fitness(mySolution);

        System.out.println(mySolution);
        System.out.println("Fitness of Hill Climbing Solution: " + hillScore + "\n");

        List<List<Integer>> evolve = gh.evolution(0);
        int geneticScore = gh.fitness(evolve);

        System.out.println(evolve);
        System.out.println("Fitness of Genetic Solution: " + geneticScore);
        //gh.fitness();
    }
}