## TASKS TO DO FOR THE APP
### Architecture
- to respect the actual structure of the files:
  - each feature has its responsibility, every of them will be mapped on my use case that I'll note here
  - a feature contain: domain - enum/classes that will be saved in the db but are the domain level. Here are put also the domain services that are validations and calculations
  - second is persistence - here are mapped the repositories that will trigger diverse action depending on the use cases 
  - the third is the application layer where will be built the use cases and the application specific services 
  - the forth is the dto - entities that will be exported in to the client
  - the fifth is the mapper - some features used for transformation of the domain -> dto & dto -> domain
  - last one api - here are created the api requests that will use the application services mandatory
### Use cases 
- (auth) the user must register, must be possible email verification for account verification, authenticate, to be something that helps in case if the user forgot the password sending email to the user and logout;
- (profile) for every user (admin & user) will be exported about portfolio data of the only profile from db (me - the user). An aggregation about: profile + contact information, top 3 projects , top 3 skills, last professional experience with brief information about it - I will prefer in some different requests;
- (skills) will be exported two kinds of the aggregation of the skills: a list of skills that will have included the name of the skill category (without the other details about this kind of entities), and another part of the individual skill where will be highlighted details about it + the projects where was used this skill
- (project) will be exported in the same way the same as the skills, but here we will respect the specifical details of this entity (challenges, features etc.) (here will be just the personal projects not the );
- (about) will have included information about the hobbies and the things that are outside the professional (volunteer, hobbies), profile I don't use it because is already used in the main page
- (experience) - here is highlighted just the professional experiences where is attached the projects from there . Another thing that will be shown is about the educational experience where I want to show brief things about it , for individual page showing courses & project from these courses;
- (contact) - the last one is about the contact, the user will send and email on my profession mail with their data , and I show to them all the details of contact about me/

### Tasks
1. make sure the auth section have implemented all I demanded , using JWT + refresh cookie;
2. Make the automated tests the all new features from the auth, organized on feature, using the same templates the same as me;
3. Having the authentication we can start with another important section: profile. Here I want to focus in 2 worlds: first in the public one where will be shown the only profile details with needed aggregation on it. The model has in view just a profile , just one so we will assume will exist just a profile that can be updated or not. if it doesn't exist this one, can be created. The rest of the entities is dependent of this one profile entity that is dependent just of the admin user. 
4. For first part of this section I want to make the automated tests how is returned the details about the profile summary, if is returned correctly and is working well.
5. Because now we can show to the users the content of our project, a good point is to have the possibility to create once the profile/update it but with an important detail: that these must be working just by an admin, other roles are blocked to do that. In this part of the profile can be created stuff present in the feature
6. Make the automated tests for the content that was built in the task 5
7. Because the profile is manageable, we can work with the skills section: I want to implement the public part that will show the skills with their categories name included in the payload, and for individual show details about them. Also, I want to include there the admin section manage: based on the profile created to be created the skills as generic entities (these will be reused by the projects lately)
8. To be sure that feature was implemented with success, I want to make the automated tests for the things created in task 7
9. Implement the project part of the application including but being aware that in the showing them I want just personal filtered, and showed. In the admin section at creation/update we must include type carefully, and can be reused by experience/hobby/volunteer without problems. Test all you implement.
10. An important part is about experience, detailed: job experience. In this case will be shown publicly details about the experience + projects that I did there. In the admin part creation, update delete. Here can be included the projects (filtered by the type of course and if is not used in other job experience.). They will be listed in desc order from the current one to the oldest one in the public. Test you implemented.
11. Fantastic