 # Gitlet Design Document

**Name**: Nitin Nazeer

# Classes and Data Structures:

## Repository:
#### Master: Points to the last node of the master branch.
#### HEAD: Points to the current location within the branch.
#### Staging Area: Stores the files that have a future addition or a future removal
#### Commits: A tree that stores the current and past commits within the git repository. It will have a branching structure.
#### Blobs: The storage location for the current and previous contents of the files within the git repository.
## Staging Area:
#### toAdd: Stores the files that are currently ready for an addition
#### toRemove: Stores the files that currently require a removal
## Commit: Represents a commit in the git repository
#### Message: the commit message
#### Timestamp: the time of the commit
#### Parent: the parent of the current commit
#### Files: holds the filenames that were staged in the commit, which includes the appropriate blob
## Blobs: The contents of a file
#### Contents: A string that holds the contents of a file at a given time.

# Algorithms:
## Init:
- Create the initial commit with null parent and UTC timestamp
- Master and HEAD point to the first Commit
- Setup the staging area and an empty list of Blobs
## Committing:
- If a file has some change made to it, that is reflected in the blob class.
- Whatever changes are made are reflected by adding to the Staging Area of the repository
- Then, clone the previous Commit
- Move the master and the HEAD pointers up to reflect the change
- Add any new files that were created from the Staging Area
- Have the Files of the Commit point to their respective blobs, which hold the current contents of the file.
## Checkout:
- Check that all files have been committed to ensure that the user doesn’t lose progress. If there are uncommitted changes, abort the operation
- Otherwise, keep the master pointer where it is, and move the HEAD pointer to the Commit with the given HASH
## Branching:
- Check if the HEAD has been moved back
- Does the new commit differ from the commit in front of it?
- If so, create a new branch in the Commits tree of the Repository, and set the parent of the new commit to point to the previous HEAD commit
- Move the HEAD pointer forward to the new commit that was created
## Merging:
- Check if the commit requires a merge operation
- If so, identify which files have a conflict
- Prompt the user to change the files that are conflicting, and then merge the branches into one commit


# Persistence:
- We will use a file system to store the objects that are required for the git repository
- The Main repository class will store all the object pointers as Strings so that we can retrieve the stored objects from the file system when necessary


