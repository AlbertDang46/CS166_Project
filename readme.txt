1. Transfer the whole folder 'phase3setup' to /tmp/$(logname)/phase3setup on 
   your assigned lab machine, where $(logname) is your user name.

2. Optional - Kill existing PostgreSQL instance by running this command:
       killall postgres
	   
3. Initialize the database instance:
       bash /tmp/$(logname)/phase3setup/postgresql/startdb.sh

4. Create tables and load data:
       bash /tmp/$(logname)/phase3setup/postgresql/createdb.sh

5. Optional: Enter SQL command windows and test with some SQL commands:
       psql -h localhost $(logname)_db
	   
6. Stop the database instance:
       bash /tmp/$(logname)/phase3setup/postgresql/stopdb.sh

psql -h localhost $(logname)_db
SELECT * FROM Users WHERE email = 'adang018@ucr.edu';
SELECT COUNT(*) FROM Bookings WHERE status = 'Pending';
SELECT COUNT(*) FROM Bookings WHERE status = 'Cancelled';

SELECT * FROM Shows WHERE sdate = '01/01/2019' AND sttime = '8:25';

SELECT C.cname, M.title, M.duration, S.sdate, S.sttime FROM Movies M, Shows S, Plays P, Theaters T, Cinemas C WHERE M.title = 'Incredibles 2' AND M.mvid = S.mvid AND S.sdate >= CAST('12/12/1995' AS DATE) AND S.sdate <= CAST('12/12/2020' AS DATE) AND S.sid = P.sid AND P.tid = T.tid AND T.cid = C.cid AND C.cname = 'Studio Movie Grill';

Incredibles 2
Studio Movie Grill

