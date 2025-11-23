# 📘 Development Diary

### 📅 30.09.25-03.10.25
- What was achieved:
	 - Composed the project idea 
	 - Began reasearching existing systems which inspired the project idea 
	 - First meeting with supervisor
- Next steps:
	 - Continue researching existing systems 
	 - Develop user stories with priorities based on research carried out 
	 - Add user stories as issues on gitlab
	 - Begin Figma designs
- Reflections/Blockers:
	 - Currently none
	 
### 📅 06.10.25-09.10.25
- What was achieved:
	 - Completed the project plan
	 - Developed user stories based on research
- Next steps:
	 - Add user stories as issues on GitLab with priorities 
	 - Begin initial Figma designs
- Reflections/Blockers:
	 - Writing the abstract took more time than I anticipated because I’m not used to producing longer written sections, so it required extra effort to structure and express my ideas clearly.
	 - Requesting feedback from my supervisor prior to submission supported my confidence that I was working in the right direction as well as provided me with necessary direction to improve my writing. 
	 
### 📅 10.10.25-12.10.25
- What was achieved:
	 - Added user stories as issues on GitLab with priorities
	 - Completed main interface designs on figma: landing page, authentication page, hover, book select, rewards page (in progress)
	 - Initialised backend using Spring Boot on IntelliJ
- Next steps:
	 - Add API connections (controller, service, model packages)
	 - Set up CI/CD pipeline
	 - Continue working on figma designs 
	 - Front-end connection
- Reflections/Blockers:
	 - No blockers but set up and design is time-consuming 
	 
### 📅 13.10.25-16.10.25
- What was achieved:
	 - End-to-end connection achieved with API connections
	 - Books fetched from Google Books API and displayed on front-end
	 - Began setting up my interim report 
- Next steps:
	 - Add test classes 
	 - Set up CI/CD pipeline
	 - Clean code: remove commented code and add javadoc 
	 - Continue working on figma designs 
	 - Background theory for interim report started
- Reflections/Blockers:
	 - I am struggling a lot with the written report and spent a lot of time worrying about perfection which affected productivity 
	 - Even if it is not perfect to start with it is better to attempt and fix later 
	 
### 📅 17.10.25-19.10.25	 
- What was achieved:
	- Structured the layout of interim report 
	- Added user stories and low-fidelity prototypes to software engineering section
	- First draft of State of Art Web Development
- Next steps:
	- Same as before (I overestimated what I could achieve)
- Reflections:
	- I finally got started with my report and feeling more confident than before
	- Now I need to make sure I am able to balance report writing with development so one doesn't hinder the productivity of the other

### 📅 20.10.25-23.10.25
- What was achieved:
	- Navbar implemented
	- BookTest, BookControllerTest (still needs fixing), SecurityConfigTest, SecurityIntegrationTest classes implemented
- Next steps:
	- Failing tests need to pass 
	- Set up CI/CD pipeline 
- Reflections:
	- I had a written assignment due for one of my modules which significantly impacted my FYP progress 
	- Still struggling to balance other modules with FYP especially with deadlines involved 

### 📅 27.10.25-29.10.25
- What was achieved:
	- No progress :(
- Reflections:
	- I had another assignment deadline for my User-Centered Design module which I didn't have time to work on other than this week, which also impacted my project progress.
 
### 📅 29.10.25-31.10.25
- What was achieved:
	- CI/CD pipeline configured
	- Fixed broken test classes 
	- Added javadoc and checkstyle  
- Next steps:
	- IDE has issues with maven checkstyle being different to Intellij's default checkstyle which needs fixing
	- IDE also does not recognise the project as a Java project so syntax colouring is incorrect -- this needs to be fixed
	- Prepare for meeting with supervisor by completing first draft of architectural design patterns and paradigms section in interim as well as cleaning up the report as best as possible 
- Reflections:
	- The pipeline set-up was very time-consuming and quite draining.
	 
### 📅 03.11.25-06.11.25
- What was achieved:
	- Intellij IDE issues solved including checkstyle config mismatches 
	- Implemented authentication page with end-to-end functionality and JWT 
	- First draft of architectural design patterns and paradigms 
	- Second mandatory meeting with supervisor: Discussed current interim and product status 
		- Main meeting takeway(s):
		--- There was a clear misunderstanding with the "background reading" section - I talked about the tools and technologies I decided to use and why rather than discussing them conceptually in the form a critical research evaluation
		--- I need to split each chapter into smaller sections to keep the focus as I tend to tangent a little 
- Next steps:
	- Books take too long to get fetched due to the authentication slowing it down -- that needs optimising
	- Need to fix bug with user accounts getting deleted when token expires 
	- Reorder some of the content which was misplaced in the interim report to their correct section
- Reflection:
	 - Before my meeting I was quite overwhelmed thinking I had underperformed and was falling behind my peers since I was struggling a lot with finding a balance between working on my FYP and keeping up with my modules at the same time, but my supervisor reassured me that feeling the pressure is normal and as long as I continue trying my best I will get far :)
	 
### 📅 07.11.25-09.11.25
- What was achieved:
	- Fixed book fetch and user accounts deleting bugs
	- Added Swiper module to allow navigating across book rows (left and right) with fade effect 
	- Redraft of architectural design patterns and paradigms section started 
	- Books that were originally fetched were quite old and academic, now they are popular thrillers and romance novels
- Next steps:
	- At the moment, each book row is identical - need to add variety and organise by category 
	- Add functionality to search bar 
	- Receive feedback from supervisor about redraft and continue optimising background reading section 
- Reflections/blockers:
	- My hard work paid off this week and I spent a lot more hours on my project, which I'm really pleased about. I feel that I am now in a solid position to keep progressing well, although I struggle a lot with writing the report so I have to prioritise that more.
	
### 📅 010.11.25-14.11.25
- What was achieved:
	- Development:
		- Added search bar functionality — currently performs local filtering to avoid repeated API calls
			- searching also normalises input (if user types a book that has a hyphen in the title without the hyphen, it still recognises it)
		- Added a fade effect when scrolling through the cards 
		- Genre tab is being worked on..currently displays some genres that a user can select and all books with that genre are displayed to the user with an animation (the animation is a bit glitchy though)
	
	- Report:
		- What are architectural paradigms and design patterns section...
			- Architectural paradigms:
				- Object-oriented Architecture complete
			- Design patterns:
				- Creational patterns: Singleton, Abstract Factory, Builder
				- Structural: Adapter, Facade, Bridge 
- Next steps:
	- For the report, I need to finish the architectural paradigms and design patterns section completely and make a start on the state of the art web development section 
	- Since I am at a decent position with my code, I am going to focus on my interim report more this week 
- Reflections: 
	- At first, developing was harder than the interim, now there has been a significant reverse -- I am very slow at writing so I am going to focus on my interim now 
	
### 📅 16.11.25-21.11.25
- What was achieved:
	- This week I worked on my report as I was falling behind
	- Completed Web development Architectural paradigms and Applicable Design patterns section 
	- Wrote introduction for state of the art web developement and completed subsection on React 
- Next steps: 
	- I need to get back into development as soon as possible so that I can achieve a couple more user stories before the deadline 
	- Complete genre tab functionality 
	- Fetch the right books in the landing page (all rows fetch the same books at the moment)
	- Add functionality for reader to be able to select a book and have the option to start reading and add it to "My library"
	- Continue working on my report -- currently working on discussing front-end technologies 
- Reflections:
	- I tend to overwrite which is slowing my progress down a lot even though I have spent many hours working on the report this week 
	- From now on, I am going to prioritise getting sections completed quicker, even if they are less thorough, and then focus on polishing and expanding if I have time left   