This project aims to solve common issues seen in software development within any industry that has to follow  V model based standard.

The V model is: @claude -  fill in a short summary.

It is required to ensure that any software developed which could have safety impact in its use is developed according to a set standard to guarntee that proper thinking and precautions have been taken and that the resulting project is relibale enough to use in action.

The issue commonly seen in practice among engineers actually developing software that should follow V model is that it is hard to understand what is needed and what the purpose of doing it is. Also a general lack of frameworks which makes it hard to create the relevant documentation.

Another aspect of development today is the rise of AI, and the fact that the models is becoming more and more compitent, and given the right circumstances will actuall be able to aid greatly even in the development of safety critical software development
* A big pain is understanding and doing documentation. AI can greatly aid with this.
* A second part, and here another of the main driving force of this project comes in is that AI performs better with more guardrails, stricter intructions and layerd design with context breaks. This is exactly what the V model provides, it is almost like the V model were made to fit development via AI agents. 

The third aspect that the project wants to adress is "Clean code" and "Clean Architecture". To give guidance in best practices for system and software development. 

All these aspects ties together. V model standars are basically advocating Clean code and clean arcitecture (with the addition of requiring some additional "proof" in terms of documentation). Agentic based development thrives with clear design from concept->system->software archtiecture->component architecture-> code.

So the aim of this project is to both adress all these points individually, but also tie them together. 
* We give templates for all the V model artifacts, to give something taht can be used as is, and if followed will fulfill the V model standars expectations. 
* We provide documentation and instruction of what the V model is, what each layer and artifact is. But together with this, for each layer we also provide best practices and "clean code" or "clean architecture".
* We provide a tool/framework, which ties the templates into a traceability graph, and a tool which can verify that everything is traced, properly coverede and also see if any change happens which might impact artifacts in higher or lower layers.
* We provide AI skills, which gives the knowledge of best practice for doing each of the levels in the V model.

All of the above could be used individually, or in any combination. 
* You could only use the AI skills if you want to have an AI with that knowledge for whatever you are doing.
* You could just read the documentation about the V model for insipration, or the Clean code best practices for knowledge without using our AI skills or templates.
* You could combine any of the above in any way or form.

But the real power comes with tying it all together
* We provide a set of AI skills/custom agents, which uses everything above. 
    * It will use the templates to create V-model compliant artifacts. 
    * It will link the artifacts with the traceability tool, and always run the tool to make sure that the traceability is maintained.
    * It will use the skills with best practice + Clean code + Clean architecture knowledge to ensure that quality products are produced
    * We will also take the best knowledge from "context engineering", making sure that each task is done focused and clear artifacts are handed over. This already ties into the V model artifacts, but in addition we will have checks to ensure that large tasks are split up and deligated to multiple seperate contexts. 

