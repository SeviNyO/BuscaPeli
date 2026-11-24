const raw = [
 ['Interstellar',2014,'Ciencia ficción',4.7,'1f3a6b','080e1a','Interstellar'],
 ['The Martian',2015,'Ciencia ficción',4.6,'7a3b14','1a0b05'],
 ['Dune',2021,'Aventura',4.5,'6b5a1f','1a1408'],
 ['Arrival',2016,'Drama',4.3,'1f5a4a','08171a'],
 ['Blade Runner 2049',2017,'Ciencia ficción',4.4,'5a2a6b','140a1e','Blade Runner'],
 ['Ex Machina',2014,'Suspenso',4.1,'2a4a6b','0b1626'],
 ['Inception',2010,'Acción',4.8,'2a4a6b','0b1626'],
 ['The Prestige',2006,'Misterio',4.5,'7a3b14','1a0b05','Prestige'],
 ['Memento',2000,'Suspenso',4.3,'1f5a4a','08171a'],
 ['Tenet',2020,'Acción',4.2,'5a2a6b','140a1e'],
 ['The Matrix',1999,'Acción',4.6,'3a4a1f','0e1408'],
 ['2001: A Space Odyssey',1968,'Ciencia ficción',4.5,'6b5a1f','1a1408','2001'],
 ['Whiplash',2014,'Drama',4.6,'6b2a2a','1a0808'],
 ['Parasite',2019,'Suspenso',4.6,'1f5a4a','08171a','Parásitos'],
 ['Gravity',2013,'Suspenso',4.5,'2a4a6b','0b1626'],
 ['Contact',1997,'Drama',4.2,'7a3b14','1a0b05'],
 ['The Dark Knight',2008,'Acción',4.7,'5a2a6b','140a1e','Dark Knight'],
 ['Pulp Fiction',1994,'Drama',4.5,'6b5a1f','1a1408'],
 ['Toy Story',1995,'Animación',4.0,'1f5a4a','08171a']
]
export const movies = raw.map(([title,year,genre,rating,a,b,posterTitle],i)=>({id:i+1,title,year,genre,rating,posterTitle:posterTitle||title,gradient:`linear-gradient(135.855deg,#${a} 14.286%,#${b} 85.714%)`}))
export const genres=['Todos','Acción','Aventura','Comedia','Drama','Ciencia ficción','Animación','Suspenso','Terror','Romance']
export const pick = (...ids)=>ids.map(id=>movies.find(m=>m.id===id)).filter(Boolean)
export const findMovie = id=>movies.find(m=>m.id===Number(id))
export const interstellar = {duration:'2 h 49 min',synopsis:'Un grupo de exploradores viaja más allá de nuestra galaxia para encontrar un nuevo hogar para la humanidad.',director:'Christopher Nolan',writer:'Jonathan Nolan',cast:[['Matthew McConaughey','Cooper'],['Anne Hathaway','Brand'],['Jessica Chastain','Murph'],['Michael Caine','Profesor Brand'],['Mackenzie Foy','Murph joven'],['Casey Affleck','Tom']]}

