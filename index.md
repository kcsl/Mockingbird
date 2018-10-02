---
layout: default
---

## Overview
The Mockingbird framework combines static and dynamic analyses to yield an efficient and scalable approach to analyze large Java software. The framework is an innovative integration of existing static and dynamic analysis tools and a newly developed component called the Object Mocker that enables the integration. The static analyzers are used to extract potentially vulnerable parts from large software. Targeted dynamic analysis (TDA) is used to analyze just the potentially vulnerable parts to check whether the vulnerability can actually be exploited.

![Workflow Diagram](/Mockingbird/workflow.png)

In the video below, we present a case study to illustrate the use of the framework to analyze complex software vulnerabilities. The case study is based on a challenge application from the DARPA Space/Time Analysis for Cybersecurity (STAC) program. Interestingly, the challenge program had been hardened and was thought not to be vulnerable. Yet, using the framework we could discover an unintentional vulnerability that can be exploited for a denial of service attack.

<center><iframe width="560" height="315" src="//www.youtube.com/embed/m9OUWtocWPE" frameborder="0" allowfullscreen></iframe></center>

## Getting Started
Ready to get started?

1. First [install](/Mockingbird/install) the Mockingbird framework static analysis tools and then [download](/Mockingbird/install#download) the Mockingbird Object Mocker.
2. Then check out the provided [tutorials](/Mockingbird/tutorials) to run the analysis.

## Source Code
Need additional resources? Checkout the [Javadocs](/Mockingbird/javadoc/index.html) or grab a copy of the [source](https://github.com/kcsl/Mockingbird).
