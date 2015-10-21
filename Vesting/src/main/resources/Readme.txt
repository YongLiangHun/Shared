1. Env: Java 7 & TestNG 6.8.21
2. The two main entity types are : Profile and Entry. A Entry could be of type VEST/PERF/SALE. A Profile consists of an employee's ID, a list of VEST records, 1 PERF record (if applicable), a list of SALE records(if applicable). Since these 3 types of entries have the same structure, a PERF's multiplier is stored in the "vestPrice" attribute.
3. The reason of putting VEST/SALE records into 2 lists if to use them as 2 queues when calculating realized gains. E.g., in the following timeline,
		V1 V2 V3 S1 V4 V5 S2 V6
   because (V1+V2+V3) >= S1, V1/V2/V3 would be polled in order until S1's sale unit amount is covered. Similarly for S2.