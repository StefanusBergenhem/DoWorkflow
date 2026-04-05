The goal with this repo is to set up a agentic workflow for industry where we have to follow DO330. A few aspects:
* It is to be inserted into already existing repos with legacy java code.
* We have to fully follow DO330/DO178/DO278, meaning that we need to deal with some requirement and design documentation
* I want to use EARS for requirement syntax
* I want us to incorperate best pracices like SOLID design and TDD.
* There might be work ongoing outside of the "workflow" that we are setting up here, meaning that we need to be able to in an organized way scan the repo and update documentation artifacts.
* There are some limitations in the models that can be used. It might be a bit older/smaller models so we have to really be crisp in instructions.


I want to do this slowly. First focusing on what is needed for making a complete argumentation for DO. I have worked with automotive before and there worked with ASPICE/ISO26262. From what I can see from DO it follows similar patterns with V model and process. So i woud rather like to focus on the common patterns, make something that can be reusable across V-model indusrty patterns. Basically work out what we need to achive
Then i want to figure out how we can just set things up in a legacy repo which have a working product, but lacks in testing and documentation artifact. or where it partly follows.
Then finally, how to set up an agentic flow. combing the normal best paractices from agentic coding, system design and V model. So instead of just TDD, it will be Design-Requirement-Test driven development.


So we will develop templates and prompts/skills for a modern agentic take on industry V model development
---------------
 Follow-Up Questions

  1. Which DO-330 TQL level are you targeting? TQL-5 (output is verified) seems most realistic for an AI agent — it
  avoids qualifying the model itself by ensuring all output is independently verified. Is that your thinking too?

  2. Requirements source of truth: In the legacy repos, do requirements currently live anywhere (DOORS, Polarion, Jama,
  spreadsheets, nowhere)? This determines whether we're creating from scratch or syncing.
  3. Java specifics: What Java version? Maven or Gradle? Any existing test framework (JUnit 4/5, TestNG)? This shapes
  the concrete templates.
  4. Safety level scope: Are we targeting the full range (DAL A through E / ASIL A through D), or a specific level?
  Higher levels require MC/DC coverage which significantly changes the tooling needs.
  5. "Work ongoing outside the workflow": Should the system detect external changes on each run and flag documentation
  drift? Or is it more of a periodic scan?
  6. Domain terminology: Should the templates use generic V-model terms (which you then map to DO/ASPICE) or should we
  have a configuration layer that lets users pick their domain vocabulary?
  7. What's the first legacy repo you'd want to pilot this on? Understanding its shape (size, test coverage,
  documentation state) would help prioritize what we build first.

  1: The intent is not to have a TQL level on the AI agent. the intent is to utalize AI coding agents to develop the tool. And we will here develop a "prompt/skill" framework for the AI coding agents. A human will also review and verify the tool/software. Do you still think that the AI agent tool and this promt/skill framework will need TQL?

  2: It can vary, requirements can live in word documents, spreadsheets, DOORS. They might be complete or incomplete or stale. Whatever exists can be provided as input in a file format of some sort

  3: JAVA 17, Gradle, Junit5. But will need extending with integration tests also. Also, ideally we keep a SOLID thinking when developing this framework. I want to be able to port it to C++ or some other poject also in the future.

  4: The difference is mostly just strictness in developint artifacts and how to follow V model. Many of these things are cheap for AI coding agents, even something that might give better output. Having more testing and guardrails in general will give better output. But lets talk more on this point.

  5: I want us to focus on developing the method of how a scan is done. Then when/how often can be something decided later on.

  6: lets do a combination. We use generic v model terms, but provide translation documentation.

  7: its bad shape. huge. I dont know how many lines but probably above a few 100 000. Testing is in bad shape, maybe 10%. Documentation is basically non-existant.

--------------------
Here is my input. Read it and summarize it back to me so that i am sure that we are on the same page. Then concider it, do additional research if needed and then come bak with your suggestions

I am leaning towards seperation of the trace file. But here it is starting to become interesting. Some general thoughts
* We need to have some system where each artifact have a truly unique ID. Some kind of hasing or UUID. Im thinking something similar to what doorstop have (if you are not familiar with doorstop, please send out a subagen to research it). But we need to scale it for the entire V model documentation artifact. And the true tracing between artifacts should be based on this
* But general use for human cannot be based on unique ID, so we need to allow for an envelope as you described also. artifact_id, artifact_type, version, status. 
* created_by human/agent. That is useless information. Who created something, if it was mark or jenny or agent nr 1 should have no impact on the overall asessment of the product and V model artifacts.
* Safety level is something that i am a bit unsure about. I am leaning towards that also being a second class pice of information. But lets talk about this. My thinking is first the naming "safety level" is not correct. The V model can also apply for quality frameworks like ASPICE. Secondly, the safety level is more of an indication of what kind of activities that needs to be taken. How do derive artifacts, what kind of checks to use and so on. But as we talked about, with agentic developmen, having stricted guidelines is generally a good thing for the output quality. So I dont really want to remove some guardrails because we dont have to use them. that could give negative impact on the quality. So this makes the safety level less important. However it is still a requirement to have safety level on requirements accoding to some standars. But maybe not on the process documentation or architectural desing artifacts. So im somewhat thinking that this should be an optional property. But please give tour input also
* reviewed by. this part is also a bit unclear to me. First of, just saying that "Mark reviewed this and said it was fine" is not going to convince any authority. These standars usually require a review checklist, review record, competence assurance of the reviewer. So im thinking first of, the checklist/review record will be the meat and bones of the skills we develop later on. How to do a good review will be the whole purpose of the skills. And maybe review record could be an output artifact that we insert into our model. Then we tie the review record artifact to the artifact that have been reviewed.
* Trace absolutely needed. We need to allow for many to many tracing. A detailed design might trace to sw architecture, to review record, to specific sw requirements, to multiple test specifications.
* So overall, I think that seperating the traceability model to its own files is where we should go. Then the actual artifacts just have a single trace to a traceability file which then handles the overal tracing that we want to do.
* And where it is becomming really interesting here is again relating to the traceability. In the end, this should be some automated check to ensure that nothing is missed. So should we make use of some existing open source thing like doorstop. Or should we as a seperate track develop our own? I am not sure if the existing things out there will cover our usecase.


I realize one more thing i want to add to the project, lets call it another minor pillar. I want to have proper documentation of how this project works. 
Questions like i ask during this project in previous session on how things actually fit together in the V model, and how we solve it in this project will be common. V model is still somewhat confusing to me after having worked with it quite a bit. And i know that it is even more confusing for many other people who never bothered to read any of the V model standars. So I want proper documentation and explantaion, not only on the tool/framework but also on the V model and how it fits together. I invision some html documentation. you should be able to grapthically see what different parts are, and how they fit togehter. Detailed explanations and multiple examples of each artifact. Some complete e2e example to follow the flow.


Lets take Pillar 3. I would like us to have a back and forth here on a high level to discus the overall plan for this pillar. Then we will need to do some research.  Create a temporary "pillar3_brainstorming.md" file where we write down things we discuss so they dont get lost. Then we have the back and forth to discuss what to research and or do. Only after we are done with the brainstorming you can start the research.

First off, I called this "skills". But that might not be the complee truth. I know that these days in the agentic workflow world there are "agents", "skills" and "commands". I need you to do some research to understand what the difference is and where to use what. I think that we will end up having a combination off all.
Then on the topic off skills, i think there are actual skills about creating skills/commands/agents. Please look into this also and recommend some for me to install. Also, one specific source of info i want you to look into is this repo: https://github.com/humanlayer/humanlayer/tree/main/.claude . It contains a workflow that seems to be really interesting to take inspiration from
Then I again, i want to have a seperatation of concern, we have some skills/commands/agents that are speciallised only for the individual topic (build, review, requirements, architecture and so on.) So completely independent of pillar 2, just provide the best practice for the craft itself. Then we have some skills/commands/agents about how to work with Pillar 1 and 2. The final set of skills/commands/agents relates to how we can create the Pillar 1 and 2 artifacts and linking in an already existing repo (my pilot).
Then there might be other things to think about for this pillar, what are we missing?

we need to reconcider. first off, the skills that we creaet shall follow mgevchev schema. 
i want to completely drop thoughts of anorchistration pipe for now. first we build individual skills, then in the future we can cocider ocistration again when we have proven the individual in practice.
We should have pure craft skills. 
then we have some "pillar1+2 integration skills". those should basically only hold output template, and some info about the framework.  prehaps some custom sctipts that will be related to pillar 2b.
then we have the repo investegation relaing to how we can create the Pillar 1 and 2 artifacts and linking in an already existing repo. Here i think we can make use of some of the paterns from humalayer.

I think i want to go from bottom up again and start by focusing only on the "craft skills"

To start with the lowest level of the V. Let me tell you what I invision:
* Skill for deriving testcases. This should contain best practices for deriving testcases as is required by most V model standars. Requirement based testing (or detailed design based testing), interface testing (or eqivlance class), fault injection. Then we also have some agentic things to concider to avoid laziness. "just testing does not throw is not a valid test, you should test actual logic". "dont mock without understanding what you are mocking", and so on.
* Skill for software development. follow TDD (with the the assumption that requirements exists). Clean code assumptions like (but not limited to) DRY, use coments sparingly, small functions, avoid deep nesting, no hidden control flow or data flow, kiss. Also strict pre-handof checklist. Lint, static code analysis, code coverate/branch coverage, integration tests. No undocumented features, and clear HALT conditions when something goes wrong
* Code review. Basically a checklist verifying that above is followed and that no weired desitions have been taken, no undocumented features and so n
* Then we have the schaffolding skill for making sure that the PILLAR 1 and 2 patterns are enforced
* Then in the layer above there should be individual skills for ceating detailed design. these skills we talk about now should assume that detailed design already exists.
* Then for agentic orchistration and keeping context window seperate i also want to have anoter skill between detailed design and coding level to basically scope the work (tell which files to touch or not)
* Finally, we need to concider how many skills are loded at a single time. Too many skills loaded will mean to many instructions and that will mean that instructions get lost. 


Well, we actually need to go back and re-work the derive-test-cases. But I think that we actually need to create 3 skills in parallel. We are creating the skill for the "code developer" agent (and in parallel best practices also for human). This contains the skills
* derive-test-cases: for covering the unit testing part of the V
* develop-code: this should cover the code implementation best practices part of the V model. Here I want a combination of what is required by the standards, what is general best practice for development. e.g "clean code", and thirdly what is best practice for AI agents when developing code

These two skills should be completely independent of our pillar 1 and 2 workflow. So here we need to rework deriving-test-cases because there are some inbuilt dependencies on pillar 1 there.

The final skill is
* develop-in-VModelWorkflow: Here we make the agent aware of Pillar 1 and 2, where to take inputs from and also what to produce (both code and artifacts like review preparation)

I need you to do some research:
- Find out what the V model standards say about software implementation.
- Find out best practices for "clean code" and how to develop good and scalable software. I like the clean code book which this is based on: https://github.com/sickn33/antigravity-awesome-skills/blob/main/skills/clean-code/SKILL.md but dont limit yourself to that webpage, just showing ths as inspiration for what kind of patterns im looking for

As always, store the research in research folder.

After that, create a best practice documentation for humans, stored in guidelines. This can be verbose, with lots of examples and explanation.

After that, get back to me, we will start looking into actually creating the 3 skills




