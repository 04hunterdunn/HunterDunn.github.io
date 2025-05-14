# The TCO Knee Study

The TCO Knee Study, the project I’ve been working on all semester, is a great example of how I’ve been able to utilize the skills I’ve learned at the University of St. Thomas and apply them to contribute to the common good—something that the University of St. Thomas is committed to.

The goal of the project was to determine which ACL reconstruction surgery did the best job rebuilding the knee back up to its intact state. However, we quickly found out that there isn’t a clear-cut “best surgery” so to speak. This is because each surgery has its benefits and limitations, which must be taken into consideration when choosing which surgery is best for various scenarios.

Each case/observation in our dataset included a cadaveric knee and the measurements for that knee at different states and trajectories. Each cadaveric knee runs through each test in its intact state, before the ACL is torn. After that, the doctors tear the ACL and run every test once again for that same knee. Following that, each knee/ACL is given ALCR surgery which undergoes each test again.

After that, the knees are split up into two groups/surgical procedures: the ACLR + ALL group and the ACLR + ML LET group. Depending on which group the knee is in, the knee will undergo every test again once their respective surgery is complete and will report its values for that group/surgical procedure. In total, we had 20 cadaveric knees that endured the process described above—10 specimens received the ACLR + ALL treatment and the other 10 received the ACLR + ML LET treatment.

Therefore, as a result, we ran multiple statistical and data analysis tasks in order to help determine the strengths and weaknesses of each ACL reconstruction surgery.

At first, we had to clean our dataset, which involved renaming observations within specific columns, removing duplicate values, and removing any rows with missing values. After that, we also transformed our observations because TCO was only concerned with changes in the knee after different surgical procedures. Therefore, we had to alter the values of every state in our dataset to be measured relative to the intact state. In other words, we took the value of each state and subtracted it from the intact state.

Thus, every value in our dataset didn’t contain the actual raw values but instead measured the difference from the intact state. Consequently, we couldn’t use ANOVA for our analysis, which was our original plan. ANOVA identifies if there is a significant difference among one of the groups in the analysis—in this case, among the different states of the ACL. However, we couldn’t utilize this analysis because ANOVA relies on comparing raw values against one another, not comparing raw values (intact state) against differences (every other ACL state).

As a result, we ran 220 one-sample t-tests in order to identify if there was a significant difference between the intact state and the other four ACL states based on every unique combination of rotation of the knee, trajectory of the knee, and flexion/angle of the knee.

In the end, we found that the ACLR + ML LET surgery did the best at replicating the intact knee and returning the knee to its normal form. However, we quickly realized that the ACL + ML LET surgery tends to overcompensate and actually restrict the knee mobility compared to the intact knee.

On the contrary, ACLR + ALL didn’t limit mobility or the motion of the knee, but it allowed for more movement and flexibility of the knee—which can be seen as a positive or a negative, depending on the situation. Thus, there isn’t a clear “best surgery” for ACL reconstruction, as every surgery has its benefits and limitations.

Doctors can use our results and the information we gathered to give patients a more detailed explanation and consequences of each ACL reconstruction surgery. As a result, patients can use this information to pick or choose the surgery they deem most appropriate based on how much knee flexibility they want and how close they want their reconstructed knee to their intact knee before tearing their ACL.

Therefore, this helps contribute to the common good—a guiding principle for the University of St. Thomas. In other words, working with TCO and my contributions to this study has helped me contribute to the positive social impact and collective well-being of our community around us, otherwise known as the Common Good.
