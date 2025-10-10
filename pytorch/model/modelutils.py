import torch
import numpy as np

def observe(model, epochs, optimizer, loss_fn, train_loader, valid_loader, batch_size, early_stopping, config):
    mean_train_losses = []
    mean_valid_losses = []
    valid_acc_list = []

    for epoch in range(epochs):
        train_loss = 0.0
        train_loss2 = 0.0
        valid_loss = 0.0
        valid_loss2 = 0.0

        model.train()

        train_losses = []
        valid_losses = []
        for i, (input, labels) in enumerate(train_loader):

            #model.zero_grad()
            optimizer.zero_grad()
            #print(i)

            outputs = model(input)
            loss = loss_fn(outputs, labels)

            loss = regularize(config, loss, model)

            loss.backward()
            optimizer.step()
            train_loss += loss.item()
            train_losses.append(loss.item())
            train_loss2 += loss.item() * input.size(0)

            #if (i * batch_size) % (batch_size * 100) == 0:
            #    print(f'{i * batch_size} / 50000')

        model.eval()
        correct = 0
        total = 0
        with torch.no_grad():
            for i, (input, labels) in enumerate(valid_loader):
                #print(i)
                outputs = model(input)
                loss = loss_fn(outputs, labels)

                valid_losses.append(loss.item())
                valid_loss += loss.item()
                valid_loss2 += loss.item() * input.size(0)

                _, predicted = torch.max(outputs.data, 1)
                correct += (predicted == labels).sum().item()
                total += labels.size(0)

        mean_train_losses.append(np.mean(train_losses))
        mean_valid_losses.append(np.mean(valid_losses))

        train_loss /= len(train_loader)
        valid_loss /= len(valid_loader)
        train_loss2 /= len(train_loader.dataset)
        valid_loss2 /= len(valid_loader.dataset)
        #val_loss /= len(valid_loader.dataset)
        #print("len valid", len(valid_loader), len(valid_loader.dataset))

        accuracy = 100 * correct / total
        valid_acc_list.append(accuracy)
        #print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
        #      .format(epoch + 1, train_loss2, valid_loss2, accuracy))
        #print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
        #      .format(epoch + 1, train_loss, valid_loss, accuracy))
        #print('epoch : {}, train loss : {:.4f}, valid loss : {:.4f}, valid acc : {:.2f}%' \
        #      .format(epoch + 1, np.mean(train_losses), np.mean(valid_losses), accuracy))

        torch.cuda.empty_cache()

        # Check early stopping condition
        # early_stopping.check_early_stop(val_loss)

        if early_stopping.stop_training:
            print(f"Early stopping at epoch {i}")
            break

def regularize(config, loss, model):
    if config.regularize:
        regularization_type = 'L2'
        lambda_reg = 0.01
        # Apply L1 regularization
        if regularization_type == 'L1':
            l1_norm = sum(p.abs().sum() for p in model.parameters())
            loss += lambda_reg * l1_norm

        # Apply L2 regularization
        elif regularization_type == 'L2':
            l2_norm = sum(p.pow(2).sum() for p in model.parameters())
            loss += lambda_reg * l2_norm

        return loss
    return loss
